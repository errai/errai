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

package org.jboss.errai.security.server;

import static org.jboss.errai.security.shared.api.identity.User.StandardUserProperties.EMAIL;
import static org.jboss.errai.security.shared.api.identity.User.StandardUserProperties.FIRST_NAME;
import static org.jboss.errai.security.shared.api.identity.User.StandardUserProperties.LAST_NAME;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.AlreadyLoggedInException;
import org.jboss.errai.security.shared.exception.AuthenticationException;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.picketlink.Identity;
import org.picketlink.Identity.AuthenticationResult;
import org.picketlink.authentication.UserAlreadyLoggedInException;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.Attribute;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.query.RelationshipQuery;

/**
 * PicketLink version of the AuthenticationService and default implementation.
 *
 * @author edewit@redhat.com
 */
@Service
@ApplicationScoped
public class PicketLinkAuthenticationService implements AuthenticationService {

  @Inject
  private Identity identity;

  @Inject
  private RelationshipManager relationshipManager;

  @Inject
  private DefaultLoginCredentials credentials;

  @Override
  public User login(String username, String password) {
    credentials.setUserId(username);
    credentials.setCredential(new Password(password));

    final AuthenticationResult result;
    
    try {
      result = identity.login();
    }
    catch (UserAlreadyLoggedInException ex) {
      throw new AlreadyLoggedInException("Already logged in as "
              + ((org.picketlink.idm.model.basic.User) identity.getAccount()).getLoginName());
    }
    catch (RuntimeException ex) {
      throw new AuthenticationException("An error occurred while authenticating.", ex);
    }

    if (result != Identity.AuthenticationResult.SUCCESS) {
      throw new FailedAuthenticationException();
    }

    final User user = createUser((org.picketlink.idm.model.basic.User) identity.getAccount(), getRolesOfCurrentUser());
    return user;
  }


  /**
   * @param picketLinkUser the user returned by picketLink
   * @param roles The roles the given user has.
   * @return our user
   */
  private User createUser(org.picketlink.idm.model.basic.User picketLinkUser, Set<? extends Role> roles) {
    User user = new UserImpl(picketLinkUser.getLoginName(), roles, translatePicketLinkAttributes(picketLinkUser));
    return user;
  }

  private Map<String, String> translatePicketLinkAttributes(
          org.picketlink.idm.model.basic.User picketLinkUser) {
    Map<String, String> result = new HashMap<String, String>();

    result.put(FIRST_NAME, picketLinkUser.getFirstName());
    result.put(LAST_NAME, picketLinkUser.getLastName());
    result.put(EMAIL, picketLinkUser.getEmail());

    for (Map.Entry<String, Attribute<? extends Serializable>> entry : picketLinkUser.getAttributesMap().entrySet()) {
      result.put(entry.getKey(), entry.getValue() == null ? null : entry.getValue().toString());
    }
    return result;
  }


  @Override
  public boolean isLoggedIn() {
    return identity.isLoggedIn();
  }

  @Override
  public void logout() {
    identity.logout();
    RpcContext.getHttpSession().invalidate();
  }

  @Override
  public User getUser() {
    if (identity.isLoggedIn()) {
      return createUser((org.picketlink.idm.model.basic.User) identity.getAccount(), getRolesOfCurrentUser());
    }
    return User.ANONYMOUS;
  }

  private Set<Role> getRolesOfCurrentUser() {
    Set<Role> roles = new HashSet<Role>();

    if (identity.isLoggedIn()) {
      RelationshipQuery<Grant> query =
              relationshipManager.createRelationshipQuery(Grant.class);
      query.setParameter(Grant.ASSIGNEE, identity.getAccount());
      for (final Grant grant : query.getResultList()) {
        roles.add(new RoleImpl(grant.getRole().getName()));
      }
    }

    return roles;
  }

}
