package org.jboss.errai.bus.server.security.auth;

import org.jboss.errai.bus.client.CommandMessage;

public interface AuthorizationAdapter {
    public void challenge(CommandMessage message);
    public boolean isAuthenticated(CommandMessage message);
    public boolean requiresAuthorization(CommandMessage message);
    public boolean endSession(CommandMessage message);
    public void process(CommandMessage message);
}
