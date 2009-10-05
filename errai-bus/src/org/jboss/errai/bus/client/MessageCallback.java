package org.jboss.errai.bus.client;

import org.jboss.errai.client.bus.CommandMessage;


public interface MessageCallback {
    public void callback(CommandMessage message);
}
