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

package org.jboss.errai.security.keycloak.mock;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.service.AuthenticationService;

public class MockWrappedAuthenticationService implements AuthenticationService {

  private User user = User.ANONYMOUS;

  @Override
  public User login(String username, String password) {
    user = new UserImpl(username);

    return user;
  }

  @Override
  public boolean isLoggedIn() {
    return !user.equals(User.ANONYMOUS);
  }

  @Override
  public void logout() {
    user = User.ANONYMOUS;
  }

  @Override
  public User getUser() {
    return user;
  }

}
