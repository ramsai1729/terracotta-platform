package org.terracotta.clientcommunicator.impl;

import org.terracotta.clientcommunicator.api.ClientBoundRequest;
import org.terracotta.clientcommunicator.api.ClientBoundRequestCodec;
import org.terracotta.clientcommunicator.api.ClientBoundRequestReceiveManager;
import org.terracotta.clientcommunicator.api.Hook;
import org.terracotta.entity.EntityClientEndpoint;
import org.terracotta.entity.EntityMessage;
import org.terracotta.entity.MessageCodecException;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author vmad
 */
public class ClientBoundRequestReceiveManagerImpl implements ClientBoundRequestReceiveManager {

    private final EntityClientEndpoint entityClientEndpoint;
    private final EntityMessageFactory entityMessageFactory;
    private final ConcurrentMap<Integer, AtomicBoolean> monitors = new ConcurrentHashMap<Integer, AtomicBoolean>();

    public ClientBoundRequestReceiveManagerImpl(EntityClientEndpoint entityClientEndpoint, EntityMessageFactory entityMessageFactory) {
        this.entityClientEndpoint = entityClientEndpoint;
        this.entityMessageFactory = entityMessageFactory;
    }

    @Override
    public void syncRequestResponseReceived(byte[] message) {
        try {
            ClientBoundRequest clientBoundRequest = ClientBoundRequestCodec.deserialize(message);

            int requestSequenceNumber = clientBoundRequest.getRequestSequenceNumber();
            AtomicBoolean last = monitors.putIfAbsent(requestSequenceNumber, new AtomicBoolean());
            if(last == null) {
                AtomicBoolean monitor = monitors.get(requestSequenceNumber);
                while (!monitor.get()) {
                    synchronized (monitor) {
                        monitor.wait();
                    }
                }
            }
            monitors.remove(requestSequenceNumber);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void clientBoundRequestReceived(byte[] entityResponse, Hook hook) {
        ClientBoundRequest clientBoundRequest = ClientBoundRequestCodec.deserialize(entityResponse);

        switch (clientBoundRequest.getRequestType()) {
            case ACK:
                hook.doSomething(clientBoundRequest.getMsgBytes());
                try {
                    entityClientEndpoint.beginInvoke().message(entityMessageFactory.createEntityMessage(ByteBuffer.allocate(4).putInt(clientBoundRequest.getRequestSequenceNumber()).array())).invoke();
                } catch (MessageCodecException e) {
                    throw new RuntimeException(e);
                }
                break;

            case NO_ACK:
                hook.doSomething(clientBoundRequest.getMsgBytes());
                break;

            case REQUEST_COMPLETE:
                int requestSequenceNumber = clientBoundRequest.getRequestSequenceNumber();
                // we are trying to add a new monitor since REQUEST_COMPLETE and CLIENT_WAIT could come out of order
                // see sendSyncMessage(...) to check how CLIENT_WAIT is handled
                AtomicBoolean last = monitors.putIfAbsent(requestSequenceNumber, new AtomicBoolean());
                if(last != null) {
                    AtomicBoolean monitor = monitors.get(requestSequenceNumber);
                    synchronized (monitor) {
                        monitor.compareAndSet(false, true);
                        monitor.notifyAll();
                    }
                }
                break;

            default:
                throw new IllegalArgumentException("unexpected/unknown ClientBoundRequestType: " + clientBoundRequest.getRequestType());
        }
    }
}

