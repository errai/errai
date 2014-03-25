/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.server;

import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Alternative;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

@Service
@ApplicationScoped
@Alternative
public class AuthenticationServiceImpl implements AuthenticationService {

  private String username;
  private User user;

  @Override
  public User login(String username, String password) {
    this.username = username;
    user = new User(username);
    user.setRoles(getRoles());

    return user;
  }

  @Override
  public boolean isLoggedIn() {
    return user != null;
  }

  @Override
  public void logout() {
    user = null;
    username = null;
  }

  @Override
  public User getUser() {
    return user;
  }

  public Set<Role> getRoles() {
    final Set<Role> roles = new HashSet<Role>();
    if (isLoggedIn()) {
      if (username.equals("admin")) {
        roles.add(new Role("admin"));
      }
      roles.add(new Role("user"));
    }

    return roles;
  }

}
