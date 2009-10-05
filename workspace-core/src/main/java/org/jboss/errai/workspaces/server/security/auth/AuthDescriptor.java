package org.jboss.errai.workspaces.server.security.auth;

import org.jboss.errai.workspaces.client.bus.CommandMessage;

public interface AuthDescriptor {
    public boolean isAuthorized(CommandMessage message);
    public void addAuthorization(Role role);
}
