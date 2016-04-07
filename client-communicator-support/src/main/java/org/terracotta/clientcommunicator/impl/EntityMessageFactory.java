package org.terracotta.clientcommunicator.impl;

import org.terracotta.clientcommunicator.api.ClientBoundRequest;
import org.terracotta.entity.EntityMessage;
import org.terracotta.entity.EntityResponse;

/**
 * @author vmad
 */
public interface EntityMessageFactory<M extends EntityMessage, R extends EntityResponse> {
    M createEntityMessage(byte[] clientBoundRequest);
    R createEntityResponse(byte[] clientBoundRequest);
}
