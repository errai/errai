package org.jboss.errai.bus.client;


public interface ErrorCallback {
    public boolean error(Message message, Throwable throwable);
}
