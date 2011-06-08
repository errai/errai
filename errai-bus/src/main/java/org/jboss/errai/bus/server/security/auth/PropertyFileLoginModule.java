/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

/**
 * This simple Login Module provides a simple authentication mechanism using property files to define users and groups.<br/>
 * <br/>
 * When in use, this LoginModule will search for <tt>users.properties</tt> and <tt>roles.properties</tt>.<br/>
 * <br/>
 * <strong>Example users.properties:</strong>
 * <tt><pre>
 * john=foo123
 * mary=abc
 * </pre></tt>
 * The left-hand value is the <em>user name</em> and the right-hand value is the <em>password</em><br/>
 * <br/>
 * <strong>Example roles.properties:</strong>
 * <tt><pre>
 * john=admin,users,finance
 * mary=users,humanResources
 * </pre></tt>
 * The left-hand value is the <em>user name</em> corresponding with the user defined in the <tt>user.properties</tt> file, and
 * the right-hand value are the roles assigned to each user.
 */
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
      this.password = new String(password.getPassword() == null ? new char[0] : password.getPassword());

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
