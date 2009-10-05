package org.jboss.errai.workspaces.server.security.auth;

import org.jboss.errai.workspaces.client.bus.CommandMessage;

public interface AuthorizationAdapter {
    public void challenge(CommandMessage message);
    public boolean isAuthenticated(CommandMessage message);
    public boolean requiresAuthorization(CommandMessage message);
    public boolean endSession(CommandMessage message);
    public void process(CommandMessage message);
}
