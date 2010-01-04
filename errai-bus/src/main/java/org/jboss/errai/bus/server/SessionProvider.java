package org.jboss.errai.bus.server;

import org.jboss.errai.bus.server.QueueSession;

public interface SessionProvider<T> {
    public QueueSession getSession(T externSessRef);
}
