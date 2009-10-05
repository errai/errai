package org.jboss.errai.workspaces.server.security.auth;

public class SimpleRole extends Role {
    public SimpleRole(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        return obj != null && (obj.equals(this) || (obj instanceof SimpleRole && ((SimpleRole) obj).getName().equals(name)));
    }
}
