/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.security.server.mock;

import java.io.Serializable;

import org.picketlink.Identity;
import org.picketlink.authentication.AuthenticationException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.Account;
import org.picketlink.idm.model.basic.User;

public class MockIdentity implements Identity {

  /**
   * When false, all login attempts fail. When true, login attempts (where credentials returns a non-null credential)
   * always succeed.
   */
  private boolean allowsLogins = true;

  private Account account = null;

  private DefaultLoginCredentials credentials;

  public MockIdentity() {
  }

  @Override
  public boolean isLoggedIn() {
    return account != null;
  }

  @Override
  public Account getAccount() {
    return account;
  }

  @Override
  public AuthenticationResult login() throws AuthenticationException {
    if (allowsLogins && credentials.getCredential() != null) {
      account = new User(credentials.getUserId());
      return AuthenticationResult.SUCCESS;
    }
    return AuthenticationResult.FAILED;
  }

  @Override
  public void logout() {
    account = null;
  }

  @Override
  public boolean hasPermission(Object resource, String operation) {
    return false;
  }

  @Override
  public boolean hasPermission(Class<?> resourceClass, Serializable identifier, String operation) {
    return false;
  }

  public void setAllowsLogins(boolean allowsLogins) {
    this.allowsLogins = allowsLogins;
  }

  public void setCredentials(DefaultLoginCredentials credentials) {
    this.credentials = credentials;
  }

  /**
   * Forces this identity into the "logged in" or "logged out" state.
   * 
   * @param user
   *          the user who this Identity will claim is logged in, or null to force "not logged in".
   */
  public void setLoggedInUser(User user) {
    account = user;
  }
}
