package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.CommandMessage;

public interface RequestDispatcher {
    public void dispatchGlobal(Message message);
    public void dispatch(Message message);
}
