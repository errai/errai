package org.jboss.errai.bus.client;

public interface MessageListener {
    public boolean handleMessage(CommandMessage message);
}
