package org.terracotta.clientcommunicator.api;

import org.terracotta.entity.EntityMessage;
import org.terracotta.entity.EntityResponse;

/**
 * @author vmad
 */
public interface ClientBoundRequestReceiveManager {
    void syncRequestResponseReceived(byte[] message);
    void clientBoundRequestReceived(byte[] message, Hook hook);
}
