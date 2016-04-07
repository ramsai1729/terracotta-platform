package org.terracotta.clientcommunicator.api;

import java.nio.ByteBuffer;

/**
 * @author vmad
 */
public class ClientBoundRequestCodec {
    public static byte[] serialize(ClientBoundRequest request) {
        int size = request.getMsgBytes().length + 12;
        ByteBuffer buffer = ByteBuffer.allocate(size).
                putInt(request.getRequestType().ordinal()).
                putInt(request.getRequestSequenceNumber()).
                put(request.getMsgBytes());
        return buffer.array();
    }
    public static ClientBoundRequest deserialize(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        ClientBoundRequestType requestType = ClientBoundRequestType.values()[buffer.getInt()];
        int requestSequenceNumber = buffer.getInt();
        byte[] msgBytes = new byte[buffer.remaining()];
        buffer.get(msgBytes);
        return new ClientBoundRequest(requestType, requestSequenceNumber, msgBytes);
    }
}
