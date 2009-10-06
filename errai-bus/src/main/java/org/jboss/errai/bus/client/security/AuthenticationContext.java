package org.jboss.errai.bus.client.security;

import java.util.Set;

public interface AuthenticationContext {
    public Set<Role> getRoles();
    public void logout();
    public boolean isValid();
}
