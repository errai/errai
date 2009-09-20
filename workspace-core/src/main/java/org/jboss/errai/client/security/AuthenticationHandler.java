package org.jboss.errai.client.security;

public interface AuthenticationHandler {
    public void doLogin(Credential[] credentials);
}
