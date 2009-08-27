package org.jboss.workspace.client.framework;

import org.jboss.workspace.client.rpc.CommandMessage;


public interface MessageCallback {
    public void callback(CommandMessage message);
}
