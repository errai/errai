package org.jboss.errai.bus.server;

import org.jboss.errai.workspaces.client.bus.CommandMessage;

public interface MessageListener {
    public boolean handleMessage(CommandMessage message);
}
