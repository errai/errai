package org.jboss.errai.workspaces.server.bus;

import org.jboss.errai.workspaces.client.bus.CommandMessage;

public interface MessageListener {
    public boolean handleMessage(CommandMessage message);
}
