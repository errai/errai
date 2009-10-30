package org.jboss.errai.bus.server.ext;

import org.jboss.errai.bus.client.CommandMessage;

public interface MessageInterceptor {
    public void intercept(CommandMessage message);
}
