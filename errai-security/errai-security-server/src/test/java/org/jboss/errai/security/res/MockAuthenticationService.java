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

package org.jboss.errai.security.res;

import java.util.List;

import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

@SuppressWarnings("unchecked")
public class MockAuthenticationService extends AbstractRpcProxy implements AuthenticationService {
  List<Role> roleList;

  public MockAuthenticationService() {
  }

  public MockAuthenticationService(List<Role> roleList) {
    this.roleList = roleList;
  }

  @Override
  public User login(String username, String password) {
    return User.ANONYMOUS;
  }

  @Override
  public boolean isLoggedIn() {
    remoteCallback.callback(Boolean.FALSE);
    return false;
  }

  @Override
  public void logout() {
  }

  @Override
  public User getUser() {
    return User.ANONYMOUS;
  }
}
