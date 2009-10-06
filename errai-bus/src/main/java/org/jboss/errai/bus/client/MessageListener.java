package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.CommandMessage;

public interface MessageListener {
    public boolean handleMessage(CommandMessage message);
}
