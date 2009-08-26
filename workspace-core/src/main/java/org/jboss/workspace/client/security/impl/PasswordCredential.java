package org.jboss.workspace.client.security.impl;

import org.jboss.workspace.client.security.Credential;

public class PasswordCredential implements Credential {
    public String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public Object getValue() {
        return password;
    }
}
