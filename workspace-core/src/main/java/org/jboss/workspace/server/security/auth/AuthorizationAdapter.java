package org.jboss.workspace.server.security.auth;

public interface AuthorizationAdapter {
    public void challenge(String name, String password);
}
