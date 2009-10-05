package org.jboss.errai.bus.server.bus;

import org.jboss.errai.client.bus.CommandMessage;

public interface MessageListener {
    public boolean handleMessage(CommandMessage message);
}
