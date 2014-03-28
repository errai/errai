package org.jboss.errai.security.server;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.jboss.errai.security.shared.api.identity.User.StandardUserProperties;
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
    assertTrue(plAuthService.getUser().hasAllRoles("meadow", "barn"));
    assertEquals(2, plAuthService.getUser().getRoles().size());
  }
}