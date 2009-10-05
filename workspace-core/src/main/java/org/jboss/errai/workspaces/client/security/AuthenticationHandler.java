package org.jboss.errai.workspaces.client.security;

public interface AuthenticationHandler {
    public void doLogin(Credential[] credentials);
}
