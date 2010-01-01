package org.jboss.errai.bus.client;


public interface ErrorCallback {
    public void error(Message message, Throwable throwable);
}
