package org.jboss.errai.server.security.auth;

public abstract class Role {
    protected Role() {
    }

    protected String name;

    public String getName() {
        return name;
    }

}
