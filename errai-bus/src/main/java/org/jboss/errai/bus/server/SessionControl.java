package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.Message;

public interface SessionControl {
    public boolean isSessionValid();
    public void activity();
}
