package org.terracotta.clientcommunicator.impl;

import org.terracotta.clientcommunicator.api.ClientBoundRequest;
import org.terracotta.clientcommunicator.api.ClientBoundRequestCodec;
import org.terracotta.clientcommunicator.api.ClientBoundRequestSendManager;
import org.terracotta.clientcommunicator.api.ClientBoundRequestType;
import org.terracotta.entity.*;

import java.nio.ByteBuffer;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author vmad
 */
public class ClientBoundRequestSendManagerImpl implements ClientBoundRequestSendManager {

    private final ClientCommunicator clientCommunicator;
    private final EntityMessageFactory entityMessageFactory;
    private final AtomicInteger requestSequence = new AtomicInteger(0);
    private final ConcurrentMap<Integer, ClientRequestInfo> pendingRequests = new ConcurrentHashMap<Integer, ClientRequestInfo>();

    public ClientBoundRequestSendManagerImpl(ClientCommunicator clientCommunicator, EntityMessageFactory entityMessageFactory) {
        this.clientCommunicator = clientCommunicator;
        this.entityMessageFactory = entityMessageFactory;
    }

    @Override
    public EntityResponse notify(Set<ClientDescriptor> connectedClients, byte[] entityResponse, ClientDescriptor client, ClientBoundRequestType requestType) throws MessageCodecException {
        int requestSequenceNumber = requestSequence.getAndIncrement();
        pendingRequests.putIfAbsent(requestSequenceNumber, new ClientRequestInfo(client, connectedClients));
        for (ClientDescriptor connectedClient : connectedClients) {
            clientCommunicator.sendNoResponse(connectedClient,
                    entityMessageFactory.createEntityResponse(ClientBoundRequestCodec.serialize(new ClientBoundRequest(requestType, requestSequenceNumber, entityResponse))));
        }

        return entityMessageFactory.createEntityResponse(ClientBoundRequestCodec.serialize(new ClientBoundRequest(ClientBoundRequestType.CLIENT_WAIT, requestSequenceNumber, new byte[0])));

    }

    @Override
    public void ackReceived(ClientDescriptor clientDescriptor, byte[] entityMessage) throws MessageCodecException {
        ByteBuffer buffer = ByteBuffer.wrap(entityMessage);
        int requestSequenceNumber = buffer.getInt();
        ClientRequestInfo clientRequestInfo = pendingRequests.get(requestSequenceNumber);
        if(clientRequestInfo != null) {
            clientRequestInfo.addAckForClient(clientDescriptor);
            if (clientRequestInfo.isAckCompleted()) {
                clientCommunicator.sendNoResponse(clientRequestInfo.getClientDescriptor(),
                        entityMessageFactory.createEntityResponse(ClientBoundRequestCodec.serialize(new ClientBoundRequest(ClientBoundRequestType.REQUEST_COMPLETE,
                                requestSequenceNumber, new byte[0]))));

                pendingRequests.remove(requestSequenceNumber);
            }
        }
    }


    private static class ClientRequestInfo {
        private final ClientDescriptor clientDescriptor;
        private final Set<ClientDescriptor> connectedClients;
        private final Set<ClientDescriptor> ackedClients = new HashSet<ClientDescriptor>();

        private ClientRequestInfo(ClientDescriptor clientDescriptor, Set<ClientDescriptor> connectedClients) {
            this.clientDescriptor = clientDescriptor;
            this.connectedClients = new HashSet<ClientDescriptor>(connectedClients);
        }

        public ClientDescriptor getClientDescriptor() {
            return clientDescriptor;
        }

        public void addAckForClient(ClientDescriptor client) {
            ackedClients.add(client);
        }

        public boolean isAckCompleted() {
            return ackedClients.equals(connectedClients);
        }
    }
}
