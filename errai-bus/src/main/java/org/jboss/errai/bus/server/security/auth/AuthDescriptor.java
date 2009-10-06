package org.jboss.errai.bus.server.security.auth;

import org.jboss.errai.bus.client.CommandMessage;


public interface AuthDescriptor {
    public boolean isAuthorized(CommandMessage message);
    public void addAuthorization(Role role);
}
