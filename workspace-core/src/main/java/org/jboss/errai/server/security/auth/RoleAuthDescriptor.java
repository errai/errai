package org.jboss.errai.server.security.auth;

import org.jboss.errai.client.bus.CommandMessage;
import org.jboss.errai.client.bus.protocols.SecurityParts;

import java.util.HashSet;
import java.util.Set;

public class RoleAuthDescriptor implements AuthDescriptor {
    private Set<Role> roles;

    public RoleAuthDescriptor(Set<Role> roles) {
        this.roles = roles;
    }

    public RoleAuthDescriptor(String[] roleNames) {
        roles = new HashSet<Role>();

        for (String role : roleNames) {
            roles.add(new SimpleRole(role));
        }
    }

    public boolean isAuthorized(CommandMessage message) {
       Set roles = message.get(Set.class, SecurityParts.Credentials);
       return roles != null && roles.equals(roles);
    }

    public void addAuthorization(Role role) {
        roles.add(role);
    }
}
