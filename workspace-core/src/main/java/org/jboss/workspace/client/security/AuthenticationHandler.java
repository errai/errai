package org.jboss.workspace.client.security;

public interface AuthenticationHandler {
    public void doLogin(Credential[] credentials);
}
