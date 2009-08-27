package org.jboss.workspace.server.bus;

import org.jboss.workspace.client.rpc.CommandMessage;

public interface MessageListener {
    public void handleMessage(CommandMessage message);
}
