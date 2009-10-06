package org.jboss.errai.bus.client.security;

public interface AuthenticationHandler {
    public void doLogin(Credential[] credentials);
}
