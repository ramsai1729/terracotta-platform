package org.terracotta.clientcommunicator.api;

/**
 * @author vmad
 */
public interface Hook {
    void doSomething(byte[] message);
}
