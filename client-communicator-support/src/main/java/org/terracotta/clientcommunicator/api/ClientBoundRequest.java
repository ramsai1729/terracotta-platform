package org.terracotta.clientcommunicator.api;

/**
 * @author vmad
 */
public class ClientBoundRequest {
    private final ClientBoundRequestType requestType;
    private final int requestSequenceNumber;
    private final byte[] msgBytes;

    public ClientBoundRequest(ClientBoundRequestType requestType, int requestSequenceNumber, byte[] msgBytes) {
        this.requestType = requestType;
        this.requestSequenceNumber = requestSequenceNumber;
        this.msgBytes = msgBytes;
    }

    public ClientBoundRequestType getRequestType() {
        return requestType;
    }

    public byte[] getMsgBytes() {
        return msgBytes;
    }

    public int getRequestSequenceNumber() {
        return requestSequenceNumber;
    }
}
