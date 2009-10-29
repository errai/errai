package org.errai.samples.serialization.client.model;

import org.jboss.errai.bus.server.annotations.ExposeEntity;

import java.io.Serializable;

@ExposeEntity
public class Role implements Serializable {
    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }
}
