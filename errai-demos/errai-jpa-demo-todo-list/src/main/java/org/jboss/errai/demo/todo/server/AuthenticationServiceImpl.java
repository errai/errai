/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.demo.todo.server;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.persistence.EntityManager;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.demo.todo.shared.TodoListUser;
import org.jboss.errai.security.shared.exception.AuthenticationException;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.picketlink.Identity;
import org.picketlink.Identity.AuthenticationResult;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.model.basic.User;

@Stateless @Service
public class AuthenticationServiceImpl implements AuthenticationService {

  @Inject private DefaultLoginCredentials credentials;
  @Inject private Identity identity;
  @Inject private EntityManager entityManager;

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public org.jboss.errai.security.shared.api.identity.User login(String username, String password) {
    credentials.setUserId(username);
    credentials.setPassword(password);
    
    final AuthenticationResult result;
    
    try {
      result = identity.login();
    }
    catch (UserAlreadyLoggedInException ex) {
      throw new UserAlreadyLoggedInException("Already logged in as "
              + ((User) identity.getAccount()).getLoginName());
    }
    catch (RuntimeException ex) {
      throw new AuthenticationException("An error occurred during authentication.", ex);
    }

    if (result == Identity.AuthenticationResult.SUCCESS) {
      final User picketLinkUser = (User) identity.getAccount();
      final TodoListUser todoListUser = lookupTodoListUser(picketLinkUser.getEmail());

      return todoListUser;
    }
    else {
      throw new FailedAuthenticationException();
    }
  }

  @Override
  public boolean isLoggedIn() {
    return identity.isLoggedIn();
  }

  @Override
  public void logout() {
    identity.logout();
  }

  @TransactionAttribute(TransactionAttributeType.REQUIRED)
  @Override
  public org.jboss.errai.security.shared.api.identity.User getUser() {
    if (identity.isLoggedIn()) {
      final User picketLinkUser = (User)identity.getAccount();
      return lookupTodoListUser(picketLinkUser.getEmail());
    }
    else {
      return org.jboss.errai.security.shared.api.identity.User.ANONYMOUS;
    }
  }
  
  private TodoListUser lookupTodoListUser(String email) {
      final TodoListUser todoListUser = entityManager
              .createNamedQuery("userByEmail", TodoListUser.class)
              .setParameter("email", email)
              .getSingleResult();

      return todoListUser;
  }
}
