package org.jboss.errai.bus.client.security.impl;

import org.jboss.errai.bus.client.security.Credential;

public class NameCredential implements Credential {
    private String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return name;
    }
}
