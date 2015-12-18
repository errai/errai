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

package org.jboss.errai.security.keycloak;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.jboss.errai.security.keycloak.mock.MockWrappedAuthenticationService;
import org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.User.StandardUserProperties;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.AlreadyLoggedInException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AccessToken.Access;
import org.keycloak.representations.AddressClaimSet;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class KeycloakAuthenticationServiceTest {

  private static final String PREFERRED_USERNAME = "john-uid";

  private static final String USER_ROLE = "user";

  private static final String ZONE_INFO = "zone";

  private static final String WEBSITE = "http://www.johndoe.com";

  private static final String STREET_ADDRESS = "123";

  private static final String REGION = "ON";

  private static final String PROFILE = "http://www.somesite.com/jd1970";

  private static final String POSTAL_CODE = "A1B2C3";

  private static final String PICTURE = "http://www.somesite.com/some-picture";

  private static final boolean PHONE_NUMBER_VERIFIED = true;

  private static final String PHONE_NUMBER = "012-345-6789";

  private static final String NICK_NAME = "Johnny";

  private static final String NAME = "John Doe";

  private static final String MIDDLE_NAME = "Anonymous";

  private static final String LOCALITY = "some locality";

  private static final String LOCALE = "en_US";

  private static final String GIVEN_NAME = "John";

  private static final String GENDER = "male";

  private static final String FORMATTED_ADDRESS = "123 Fake Street, Toronto, Ontario, Canada";

  private static final String FAMILY_NAME = "Doe";

  private static final boolean EMAIL_VERIFIED = true;

  private static final String EMAIL = "john@doe.com";

  private static final String COUNTRY = "Canada";

  private static final String BIRTHDATE = "January 1st, 1970";

  private static final String ERRAI_APP = "http://some-errai-app";

  private AssertMap keycloakAssertionMap;

  private AssertMap wrappedAssertionMap;

  @InjectMocks
  private KeycloakAuthenticationService authService;

  @Spy
  private MockWrappedAuthenticationService mockWrappedService;

  private KeycloakSecurityContext securityContext;

  @Before
  public void setup() {
    final AccessToken accessToken = new AccessToken();
    
    setUserProperties(accessToken);

    // Token strings are never used
    securityContext = new KeycloakSecurityContext("", accessToken, "", accessToken);

    keycloakAssertionMap = new AssertMap(new Assertion() {
      @Override
      public void doAssertion(final Object expected, final Object observed) {
        assertEquals(expected, observed);
      }
    });

    wrappedAssertionMap = new AssertMap(new Assertion() {
      @Override
      public void doAssertion(Object expected, Object observed) {
        assertNull(observed);
      }
    });
  }

  private void setUserProperties(final AccessToken accessToken) {
    accessToken.setPreferredUsername(PREFERRED_USERNAME);

    accessToken.setAddress(new AddressClaimSet());
    accessToken.setBirthdate(BIRTHDATE);
    accessToken.getAddress().setCountry(COUNTRY);
    accessToken.setEmail(EMAIL);
    accessToken.setEmailVerified(EMAIL_VERIFIED);
    accessToken.setFamilyName(FAMILY_NAME);
    accessToken.getAddress().setFormattedAddress(FORMATTED_ADDRESS);
    accessToken.setGender(GENDER);
    accessToken.setGivenName(GIVEN_NAME);
    accessToken.setLocale(LOCALE);
    accessToken.getAddress().setLocality(LOCALITY);
    accessToken.setMiddleName(MIDDLE_NAME);
    accessToken.setName(NAME);
    accessToken.setNickName(NICK_NAME);
    accessToken.setPhoneNumber(PHONE_NUMBER);
    accessToken.setPhoneNumberVerified(PHONE_NUMBER_VERIFIED);
    accessToken.setPicture(PICTURE);
    accessToken.getAddress().setPostalCode(POSTAL_CODE);
    accessToken.setProfile(PROFILE);
    accessToken.getAddress().setRegion(REGION);
    accessToken.getAddress().setStreetAddress(STREET_ADDRESS);
    accessToken.setWebsite(WEBSITE);
    accessToken.setZoneinfo(ZONE_INFO);

    setUserRoles(accessToken);
  }

  private void setUserRoles(final AccessToken accessToken) {
    final Access access = new Access();
    access.addRole(USER_ROLE);
    
    Map<String, Access> resourceAccess = new HashMap<String, Access>();
    resourceAccess.put(ERRAI_APP, access);
    accessToken.issuedFor(ERRAI_APP);
    accessToken.setResourceAccess(resourceAccess);
  }

  private void verifyUserProperties(final User user, final AssertMap map) {
    assertEquals(PREFERRED_USERNAME, user.getIdentifier());

    map.get(KeycloakPropertyNames.BIRTHDATE).doAssertion(BIRTHDATE, user.getProperty(KeycloakPropertyNames.BIRTHDATE));
    map.get(KeycloakPropertyNames.COUNTRY).doAssertion(COUNTRY, user.getProperty(KeycloakPropertyNames.COUNTRY));
    map.get(StandardUserProperties.EMAIL).doAssertion(EMAIL, user.getProperty(StandardUserProperties.EMAIL));
    map.get(KeycloakPropertyNames.EMAIL_VERIFIED).doAssertion(String.valueOf(EMAIL_VERIFIED),
            user.getProperty(KeycloakPropertyNames.EMAIL_VERIFIED));
    map.get(StandardUserProperties.LAST_NAME).doAssertion(FAMILY_NAME,
            user.getProperty(StandardUserProperties.LAST_NAME));
    map.get(KeycloakPropertyNames.FORMATTED_ADDRESS).doAssertion(FORMATTED_ADDRESS,
            user.getProperty(KeycloakPropertyNames.FORMATTED_ADDRESS));
    map.get(KeycloakPropertyNames.GENDER).doAssertion(GENDER, user.getProperty(KeycloakPropertyNames.GENDER));
    map.get(StandardUserProperties.FIRST_NAME).doAssertion(GIVEN_NAME,
            user.getProperty(StandardUserProperties.FIRST_NAME));
    map.get(KeycloakPropertyNames.LOCALE).doAssertion(LOCALE, user.getProperty(KeycloakPropertyNames.LOCALE));
    map.get(KeycloakPropertyNames.LOCALITY).doAssertion(LOCALITY, user.getProperty(KeycloakPropertyNames.LOCALITY));
    map.get(KeycloakPropertyNames.MIDDLE_NAME).doAssertion(MIDDLE_NAME,
            user.getProperty(KeycloakPropertyNames.MIDDLE_NAME));
    map.get(KeycloakPropertyNames.NAME).doAssertion(NAME, user.getProperty(KeycloakPropertyNames.NAME));
    map.get(KeycloakPropertyNames.NICKNAME).doAssertion(NICK_NAME, user.getProperty(KeycloakPropertyNames.NICKNAME));
    map.get(KeycloakPropertyNames.PHONENUMBER).doAssertion(PHONE_NUMBER,
            user.getProperty(KeycloakPropertyNames.PHONENUMBER));
    map.get(KeycloakPropertyNames.PHONENUMBER_VERIFIED).doAssertion(String.valueOf(PHONE_NUMBER_VERIFIED),
            user.getProperty(KeycloakPropertyNames.PHONENUMBER_VERIFIED));
    map.get(KeycloakPropertyNames.PICTURE).doAssertion(PICTURE, user.getProperty(KeycloakPropertyNames.PICTURE));
    map.get(KeycloakPropertyNames.POSTAL_CODE).doAssertion(POSTAL_CODE,
            user.getProperty(KeycloakPropertyNames.POSTAL_CODE));
    map.get(KeycloakPropertyNames.PREFERRED_USERNAME).doAssertion(PREFERRED_USERNAME,
            user.getProperty(KeycloakPropertyNames.PREFERRED_USERNAME));
    map.get(KeycloakPropertyNames.PROFILE).doAssertion(PROFILE, user.getProperty(KeycloakPropertyNames.PROFILE));
    map.get(KeycloakPropertyNames.REGION).doAssertion(REGION, user.getProperty(KeycloakPropertyNames.REGION));
    map.get(KeycloakPropertyNames.STREET_ADDRESS).doAssertion(STREET_ADDRESS,
            user.getProperty(KeycloakPropertyNames.STREET_ADDRESS));
    map.get(KeycloakPropertyNames.WEBSITE).doAssertion(WEBSITE, user.getProperty(KeycloakPropertyNames.WEBSITE));
    map.get(KeycloakPropertyNames.ZONE_INFO).doAssertion(ZONE_INFO, user.getProperty(KeycloakPropertyNames.ZONE_INFO));
  }

  private void verifyUserRoles(final UserImpl user, final String... roleNames) {
    assertTrue(user.hasAllRoles(roleNames));
  }

  @Test
  public void isLoggedInReturnsFalseWhenNoUserLoggedIn() throws Exception {
    assertFalse(authService.isLoggedIn());
  }

  @Test
  public void getUserReturnsAnonymousWhenNoUserLoggedIn() throws Exception {
    assertEquals(User.ANONYMOUS, authService.getUser());
  }

  @Test
  public void isLoggedInReturnsTrueWhenKeycloakUserLoggedIn() throws Exception {
    authService.setSecurityContext(securityContext);

    assertTrue(authService.isLoggedIn());
  }

  @Test
  public void getUserReturnsUserWhenKeycloakUserLoggedIn() throws Exception {
    authService.setSecurityContext(securityContext);

    final UserImpl user = (UserImpl) authService.getUser();
    verifyUserProperties(user, keycloakAssertionMap);
    verifyUserRoles(user, USER_ROLE);
  }

  @Test
  public void isLoggedInReturnsTrueWhenWrappedUserLoggedIn() throws Exception {
    mockWrappedService.login(PREFERRED_USERNAME, "");

    assertTrue(authService.isLoggedIn());
  }

  @Test
  public void getUserReturnsUserWhenWrappedUserLoggedIn() throws Exception {
    mockWrappedService.login(PREFERRED_USERNAME, "");

    final UserImpl user = (UserImpl) authService.getUser();
    verifyUserProperties(user, wrappedAssertionMap);
    verifyUserRoles(user);
  }

  @Test
  public void isLoggedInReturnsFalseAfterKeycloakUserLogsOut() throws Exception {
    // setup
    isLoggedInReturnsTrueWhenKeycloakUserLoggedIn();
    authService.logout();

    assertFalse(authService.isLoggedIn());
  }

  @Test
  public void getUserReturnsAnonymousAfterKeycloakUserLogsOut() throws Exception {
    getUserReturnsUserWhenKeycloakUserLoggedIn();
    authService.logout();

    assertEquals(User.ANONYMOUS, authService.getUser());
  }

  @Test
  public void isLoggedInReturnsFalseAfterWrappedUserLogsOut() throws Exception {
    isLoggedInReturnsTrueWhenWrappedUserLoggedIn();
    authService.logout();

    assertFalse(authService.isLoggedIn());
  }

  @Test
  public void getUserReturnsAnonymousAfterWrappedUserLogsOut() throws Exception {
    getUserReturnsUserWhenWrappedUserLoggedIn();
    authService.logout();

    assertEquals(User.ANONYMOUS, authService.getUser());
  }

  @Test
  public void logoutIsIdempotent() throws Exception {
    authService.logout();
    authService.logout();

    isLoggedInReturnsFalseWhenNoUserLoggedIn();
  }

  @Test(expected = AlreadyLoggedInException.class)
  public void loginFailsIfKeycloakUserLoggedIn() throws Exception {
    getUserReturnsUserWhenKeycloakUserLoggedIn();

    authService.login(PREFERRED_USERNAME, "");
  }

  @Test(expected = AlreadyLoggedInException.class)
  public void settingKeycloakUserFailsIfWrappedUserLoggedIn() throws Exception {
    getUserReturnsUserWhenWrappedUserLoggedIn();

    authService.setSecurityContext(securityContext);
  }

  @Test
  public void createdKeycloakUserDoesNotHaveUnavailableProperties() throws Exception {
    securityContext.getToken().setLocale(null);
    keycloakAssertionMap.put(KeycloakPropertyNames.LOCALE, new Assertion() {
      @Override
      public void doAssertion(final Object expected, final Object observed) {
        assertNull(observed);
      }
    });

    getUserReturnsUserWhenKeycloakUserLoggedIn();
  }

  private static class AssertMap {
    private final Assertion defaultAssertion;
    private final Map<Object, Assertion> assertions;

    AssertMap(final Assertion defaultComparison) {
      this.defaultAssertion = defaultComparison;
      assertions = new HashMap<Object, KeycloakAuthenticationServiceTest.Assertion>();
    }

    Assertion get(final Object key) {
      final Assertion assertion = assertions.get(key);
      if (assertion == null) {
        return defaultAssertion;
      }
      else {
        return assertion;
      }
    }

    void put(final Object key, final Assertion newAssertion) {
      assertions.put(key, newAssertion);
    }
  }

  private interface Assertion {
    void doAssertion(Object expected, Object observed);
  }

}
