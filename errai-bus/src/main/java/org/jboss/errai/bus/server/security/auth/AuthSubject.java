package org.jboss.errai.bus.server.security.auth;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class AuthSubject {
    protected String username;
    protected String fullname;
    protected Collection<Object> roles;

    public AuthSubject(String username, String fullname, Collection<Object> roles) {
        this.username = username;
        this.fullname = fullname;
        this.roles = roles;
    }

    public String getUsername() {
        return username;
    }

    public String getFullname() {
        return fullname;
    }

    public Collection<Object> getRoles() {
        return roles;
    }

    public String toRolesString() {
        StringBuilder builder = new StringBuilder();
        Iterator<Object> iter = roles.iterator();
        while (iter.hasNext()) {
            builder.append(String.valueOf(iter.next()));
            if (iter.hasNext()) builder.append(",");
        }

        return builder.toString();
    }
}
