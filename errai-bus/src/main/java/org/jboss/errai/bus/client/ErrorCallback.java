package org.jboss.errai.bus.client;


public interface ErrorCallback {
    public void error(CommandMessage message, Throwable throwable);
}
