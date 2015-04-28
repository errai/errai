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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import org.jboss.errai.security.shared.api.RequiredRolesProvider;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User.StandardUserProperties;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.AuthenticationException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.RelationshipManager;
import org.picketlink.idm.model.IdentityType;
import org.picketlink.idm.query.RelationshipQuery;

@RunWith(MockitoJUnitRunner.class)
public class PicketLinkUserMappingTest {

  @Mock Identity mockIdentity;
  @Mock RelationshipManager mockRelationshipManager;
  @Mock DefaultLoginCredentials mockLoginCredentials;
  @Mock RequiredRolesProvider requiredRolesProvider;
  @Mock PicketLinkBasicModelServices basicModelServices;

  @SuppressWarnings("rawtypes")
  @Mock RelationshipQuery mockQuery;

  @InjectMocks
  private PicketLinkAuthenticationService plAuthService;

  /**
   * Sets up generic behavior that's mostly right for all the tests: you can log
   * in successfully with any credentials, and the resulting user has a first
   * name, last name, email address, and no group memberships.
   * <p>
   * Look at the tests to see how to override these things on an individual basis.
   */
  @SuppressWarnings("unchecked")
  @Before
  public void setupMocks() {
    org.picketlink.idm.model.basic.User plAccount = new org.picketlink.idm.model.basic.User("cow");
    plAccount.setEmail("cow@moo");
    plAccount.setFirstName("Cowtest");
    plAccount.setLastName("Mootest");

    when(mockIdentity.getAccount()).thenReturn(plAccount);
    when(mockIdentity.login()).thenReturn(Identity.AuthenticationResult.SUCCESS);
    when(mockIdentity.isLoggedIn()).thenReturn(true);

    when(mockQuery.getResultList()).thenReturn(Collections.emptyList());
    when(mockRelationshipManager.createRelationshipQuery(any(Class.class))).thenReturn(mockQuery);
    when(requiredRolesProvider.getRoles()).thenReturn(Collections.<org.jboss.errai.security.shared.api.Role> emptySet());
    when(basicModelServices.hasRole(any(IdentityType.class), any(String.class))).thenReturn(false);
  }

  @Test
  public void loginShouldCreateAUserWithTheCorrectId() {
    plAuthService.login("cow", "moo");
    assertEquals("cow", plAuthService.getUser().getIdentifier());
  }

  @Test(expected=AuthenticationException.class)
  public void failedLoginShouldThrowException() {
    when(mockIdentity.login()).thenReturn(Identity.AuthenticationResult.FAILED);
    plAuthService.login("cow", "moo");
  }

  @Test
  public void loginShouldMapSpecialAttributesToErraiEquivalents() {
    plAuthService.login("cow", "moo");
    assertEquals("Cowtest", plAuthService.getUser().getProperty(StandardUserProperties.FIRST_NAME));
    assertEquals("Mootest", plAuthService.getUser().getProperty(StandardUserProperties.LAST_NAME));
    assertEquals("cow@moo", plAuthService.getUser().getProperty(StandardUserProperties.EMAIL));
  }

  @Test
  public void loginShouldRetainRolesFromPicketLink() {
    HashSet<org.jboss.errai.security.shared.api.Role> erraiRoles = new HashSet<org.jboss.errai.security.shared.api.Role>(Arrays.asList(new RoleImpl("meadow"), new RoleImpl("barn")));
    when(requiredRolesProvider.getRoles()).thenReturn(erraiRoles);

    when(basicModelServices.hasRole(any(IdentityType.class), eq("meadow"))).thenReturn(true);
    when(basicModelServices.hasRole(any(IdentityType.class), eq("barn"))).thenReturn(true);

    plAuthService.login("cow", "moo");
    assertTrue(((UserImpl)plAuthService.getUser()).hasAllRoles("meadow", "barn"));
    assertEquals(2, plAuthService.getUser().getRoles().size());
  }

  @Test
  public void loginShouldRetainRolesFromPicketLinkThatUserActuallyHas() {
    HashSet<org.jboss.errai.security.shared.api.Role> erraiRoles = new HashSet<org.jboss.errai.security.shared.api.Role>(Arrays.asList(new RoleImpl("meadow"), new RoleImpl("barn")));
    when(requiredRolesProvider.getRoles()).thenReturn(erraiRoles);

    when(basicModelServices.hasRole(any(IdentityType.class), eq("meadow"))).thenReturn(false);
    when(basicModelServices.hasRole(any(IdentityType.class), eq("barn"))).thenReturn(true);

    plAuthService.login("cow", "moo");
    assertTrue(((UserImpl) plAuthService.getUser()).hasAllRoles("barn"));
    assertEquals(1, plAuthService.getUser().getRoles().size());
  }
}