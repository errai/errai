package org.jboss.workspace.server.security.auth;

import org.jboss.workspace.client.rpc.CommandMessage;

public interface AuthDescriptor {
    public boolean isAuthorized(CommandMessage message);
    public void addAuthorization(Role role);
}
