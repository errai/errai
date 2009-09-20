package org.jboss.errai.client.security.impl;

import org.jboss.errai.client.security.Credential;

public class PasswordCredential implements Credential {
    public String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public Object getValue() {
        return password;
    }
}
