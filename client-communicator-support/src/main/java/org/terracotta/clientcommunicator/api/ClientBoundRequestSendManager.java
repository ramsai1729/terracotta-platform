package org.terracotta.clientcommunicator.api;

import org.terracotta.entity.ClientDescriptor;
import org.terracotta.entity.EntityMessage;
import org.terracotta.entity.EntityResponse;
import org.terracotta.entity.MessageCodecException;

import java.util.Set;

/**
 * @author vmad
 */
public interface ClientBoundRequestSendManager {
    EntityResponse notify(Set<ClientDescriptor> clientDescriptors, byte[] entityResponse, ClientDescriptor clientDescriptor, ClientBoundRequestType requestType) throws MessageCodecException;
    void ackReceived(ClientDescriptor clientDescriptor, byte[] entityMessage) throws MessageCodecException;
}
