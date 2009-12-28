package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.CommandMessage;

public interface RequestDispatcher {
    public void dispatchGlobal(CommandMessage message);
    public void dispatch(CommandMessage message);
}
