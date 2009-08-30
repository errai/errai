package org.jboss.workspace.server.security.auth;

import org.jboss.workspace.client.rpc.CommandMessage;
import org.jboss.workspace.client.rpc.protocols.SecurityParts;

import java.util.Set;
import java.util.HashSet;

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
