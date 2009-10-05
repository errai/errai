package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.CommandMessage;

public interface MessageListener {
    public boolean handleMessage(CommandMessage message);
}
