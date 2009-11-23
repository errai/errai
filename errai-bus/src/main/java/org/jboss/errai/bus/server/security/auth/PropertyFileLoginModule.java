/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.security.auth;

import javax.security.auth.Subject;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.security.Principal;
import java.util.Map;
import java.util.ResourceBundle;

public class PropertyFileLoginModule implements LoginModule {
    private String login;
    private String password;
    private Subject subject;

    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        NameCallback name = new NameCallback("Login");
        PasswordCallback password = new PasswordCallback("Password", false);
        this.subject = subject;

        try {
            callbackHandler.handle(new Callback[]{name, password});
            this.login = name.getName();
            this.password = new String(password.getPassword());

            loadRoles();

        }
        catch (Exception e) {
            throw new RuntimeException("could not login", e);
        }
    }

    public boolean login() throws LoginException {
        ResourceBundle bundle = ResourceBundle.getBundle("users");
        String password = bundle.getString(login);
        return password != null && this.password.equals(password);
    }

    public void loadRoles() {
        ResourceBundle bundle = ResourceBundle.getBundle("roles");
        String[] roles = bundle.getString(login).split(",");

        for (final String role : roles) {
            subject.getPrincipals().add(new Principal() {
                private String name = role.trim();

                public String getName() {
                    return name;
                }

                @Override
                public boolean equals(Object obj) {
                    return name.equals(obj);
                }

                @Override
                public String toString() {
                    return name;
                }
            });
        }


    }

    public boolean commit() throws LoginException {
        return true;
    }

    public boolean abort() throws LoginException {
        return true;
    }

    public boolean logout() throws LoginException {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
