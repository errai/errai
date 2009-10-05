package org.jboss.errai.client.framework;

import org.jboss.errai.client.bus.CommandMessage;


public interface MessageCallback {
    public void callback(CommandMessage message);
}
