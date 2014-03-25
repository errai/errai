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
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.AuthenticationException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.query.RelationshipQuery;

/**
 * PicketLink version of the AuthenticationService and default implementation.
 * To change implementations put a alternatives into your beans.xml
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

    if (identity.login() != Identity.AuthenticationResult.SUCCESS) {
      throw new AuthenticationException();
    }

    final User user = createUser((org.picketlink.idm.model.basic.User) identity.getAccount(), getRoles());
    return user;
  }


  /**
   * @param picketLinkUser the user returned by picketLink
   * @param roles The roles the given user has.
   * @return our user
   */
  private User createUser(org.picketlink.idm.model.basic.User picketLinkUser, Set<Role> roles) {
    User user = new User();
    user.setLoginName(picketLinkUser.getLoginName());
    user.setLastName(picketLinkUser.getLastName());
    user.setFirstName(picketLinkUser.getFirstName());
    user.setEmail(picketLinkUser.getEmail());
    user.setRoles(roles);
    return user;
  }

  @Override
  public boolean isLoggedIn() {
    return identity.isLoggedIn();
  }

  @Override
  public void logout() {
    identity.logout();
  }

  @Override
  public User getUser() {
    if (identity.isLoggedIn()) {
      return createUser((org.picketlink.idm.model.basic.User) identity.getAccount(), getRoles());
    }
    return null;
  }

  public Set<Role> getRoles() {
    Set<Role> roles = new HashSet<Role>();

    if (identity.isLoggedIn()) {
      RelationshipQuery<Grant> query =
              relationshipManager.createRelationshipQuery(Grant.class);
      query.setParameter(Grant.ASSIGNEE, identity.getAccount());
      for (final Grant grant : query.getResultList()) {
        roles.add(new Role(grant.getRole().getName()));
      }
    }

    return roles;
  }

}
