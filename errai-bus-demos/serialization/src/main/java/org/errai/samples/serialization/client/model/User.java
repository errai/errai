package org.errai.samples.serialization.client.model;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.io.Serializable;
import java.util.Set;

@ExposeEntity
public class User implements Serializable {
    private int userId;
    private String userName;
    private String password;
    private Set<Role> roles;

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }
}
