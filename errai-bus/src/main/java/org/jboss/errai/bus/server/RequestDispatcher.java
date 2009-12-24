package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.CommandMessage;

public interface RequestDispatcher {
    public void deliver(CommandMessage message);
}
