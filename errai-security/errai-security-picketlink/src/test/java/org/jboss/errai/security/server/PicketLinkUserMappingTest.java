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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

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
import org.picketlink.idm.model.basic.Grant;
import org.picketlink.idm.model.basic.Role;
import org.picketlink.idm.query.RelationshipQuery;

@RunWith(MockitoJUnitRunner.class)
public class PicketLinkUserMappingTest {

  @Mock Identity mockIdentity;
  @Mock RelationshipManager mockRelationshipManager;
  @Mock DefaultLoginCredentials mockLoginCredentials;

  @SuppressWarnings("rawtypes")
  @Mock RelationshipQuery mockQuery;

  @InjectMocks
  private PicketLinkAuthenticationService plAuthService;

  /**
   * Sets up generic behaviour that's mostly right for all the tests: you can
   * log in successfully with any credentials, and the resulting user has a
   * first name, last name, email address, and no group memberships.
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
    when(mockRelationshipManager.createRelationshipQuery( any(Class.class) )).thenReturn(mockQuery);
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
    List<Grant> plRoles = Arrays.asList(
            new Grant(null, new Role("meadow")),
            new Grant(null, new Role("barn")));
    when(mockQuery.getResultList()).thenReturn(plRoles);

    plAuthService.login("cow", "moo");
    assertTrue(((UserImpl)plAuthService.getUser()).hasAllRoles("meadow", "barn"));
    assertEquals(2, plAuthService.getUser().getRoles().size());
  }
}
