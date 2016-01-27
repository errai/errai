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

import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.AUDIENCE;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.BIRTHDATE;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.COUNTRY;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.EMAIL_VERIFIED;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.FORMATTED_ADDRESS;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.GENDER;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.LOCALE;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.LOCALITY;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.MIDDLE_NAME;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.NAME;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.NICKNAME;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.PHONENUMBER;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.PHONENUMBER_VERIFIED;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.PICTURE;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.POSTAL_CODE;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.PREFERRED_USERNAME;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.PROFILE;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.REGION;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.STREET_ADDRESS;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.SUBJECT;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.WEBSITE;
import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.ZONE_INFO;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.inject.Inject;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.bus.server.api.RpcContext;
import org.jboss.errai.security.keycloak.extension.Filtered;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.User.StandardUserProperties;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.AlreadyLoggedInException;
import org.jboss.errai.security.shared.exception.AuthenticationException;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;
import org.keycloak.representations.AddressClaimSet;

/**
 * <p>
 * An {@link AuthenticationService} implementation that integrates with Keycloak. This
 * implementation optionally wraps another {@link AuthenticationService} so that an app can have
 * local and foreign user authentication.
 *
 * <p>
 * Some important behaviour of this implementation:
 * <ul>
 * <li>The {@link #login(String, String)} method throws an {@link FailedAuthenticationException} if
 * there is no wrapped {@link AuthenticationService}.
 * <li>Attempting to login (through Keycloak or the wrapped service) while a user is already logged
 * in causes an exception.
 * <li>After a Keycloak login, what properties the {@link User} has depends on which properties have
 * been enabled in Keycloak. The user instance returned will have all values from the Keycloak
 * {@link AccessToken} pertaining to the user that were not null.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@Service
@SessionScoped
public class KeycloakAuthenticationService implements AuthenticationService, Serializable {

  private static class KeycloakProperty {
    final String name;
    final String value;

    KeycloakProperty(final String name, final String value) {
      this.name = name;
      this.value = value;
    }

    boolean hasValue() {
      return value != null;
    }
  }

  private static final long serialVersionUID = 1L;

  @Inject
  @Filtered
  private AuthenticationService wrappedAuthService;

  private User keycloakUser;

  private KeycloakSecurityContext keycloakSecurityContext;

  @Override
  public User login(final String username, final String password) {
    if (!keycloakIsLoggedIn()) {
      return wrappedAuthService.login(username, password);
    }
    else {
      throw new AlreadyLoggedInException("Already logged in through Keycloak.");
    }
  }

  @Override
  public boolean isLoggedIn() {
    return keycloakIsLoggedIn() || wrappedAuthService.isLoggedIn();
  }

  private boolean keycloakIsLoggedIn() {
    return keycloakSecurityContext != null && keycloakSecurityContext.getToken() != null;
  }

  @Override
  public void logout() {
    if (keycloakIsLoggedIn()) {
      keycloakLogout();
      
      try {
        if (RpcContext.getMessage() != null)
          ((HttpServletRequest) RpcContext.getServletRequest()).logout();
      } catch (ServletException e) {
        throw new AuthenticationException("An error occurred while attempting to log out of Keycloak.");
      }
    }
    else if (wrappedAuthService.isLoggedIn()) {
      wrappedAuthService.logout();
    }
  }

  private void keycloakLogout() {
    setSecurityContext(null);
  }

  @Override
  public User getUser() {
    if (keycloakIsLoggedIn()) {
      return getKeycloakUser();
    }
    else if (wrappedAuthService.isLoggedIn()) {
      return wrappedAuthService.getUser();
    }
    else {
      return User.ANONYMOUS;
    }
  }

  private User getKeycloakUser() {
    if (!keycloakIsLoggedIn()) {
      throw new IllegalStateException(
              "Cannot call getKeycloakUser if not logged in through Keycloak.");
    }

    if (keycloakUser == null) {
      keycloakUser = createKeycloakUser(keycloakSecurityContext.getToken());
    }

    return keycloakUser;
  }

  protected User createKeycloakUser(final AccessToken accessToken) {
    final User user = new UserImpl(accessToken.getPreferredUsername(), createRoles(accessToken));

    final Collection<KeycloakProperty> properties = getKeycloakUserProperties(accessToken);

    for (KeycloakProperty property : properties) {
      if (property.hasValue()) {
        user.setProperty(property.name, property.value);
      }
    }

    return user;
  }

  private Collection<KeycloakProperty> getKeycloakUserProperties(final AccessToken accessToken) {
    final Collection<KeycloakProperty> properties = new ArrayList<KeycloakAuthenticationService.KeycloakProperty>();

    properties.add(new KeycloakProperty(StandardUserProperties.FIRST_NAME, accessToken.getGivenName()));
    properties.add(new KeycloakProperty(StandardUserProperties.LAST_NAME, accessToken.getFamilyName()));
    properties.add(new KeycloakProperty(StandardUserProperties.EMAIL, accessToken.getEmail()));
    properties.add(new KeycloakProperty(BIRTHDATE, accessToken.getBirthdate()));
    properties.add(new KeycloakProperty(GENDER, accessToken.getGender()));
    properties.add(new KeycloakProperty(LOCALE, accessToken.getLocale()));
    properties.add(new KeycloakProperty(MIDDLE_NAME, accessToken.getMiddleName()));
    properties.add(new KeycloakProperty(NAME, accessToken.getName()));
    properties.add(new KeycloakProperty(NICKNAME, accessToken.getNickName()));
    properties.add(new KeycloakProperty(PHONENUMBER, accessToken.getPhoneNumber()));
    properties.add(new KeycloakProperty(PICTURE, accessToken.getPicture()));
    properties.add(new KeycloakProperty(PREFERRED_USERNAME, accessToken.getPreferredUsername()));
    properties.add(new KeycloakProperty(PROFILE, accessToken.getProfile()));
    properties.add(new KeycloakProperty(SUBJECT, accessToken.getSubject()));
    properties.add(new KeycloakProperty(WEBSITE, accessToken.getWebsite()));
    properties.add(new KeycloakProperty(ZONE_INFO, accessToken.getZoneinfo()));
    properties.add(new KeycloakProperty(EMAIL_VERIFIED, String.valueOf(accessToken.getEmailVerified())));
    properties.add(new KeycloakProperty(PHONENUMBER_VERIFIED, String.valueOf(accessToken.getPhoneNumberVerified())));

    // populate address properties
    AddressClaimSet address = accessToken.getAddress();
      if (address != null) {
        properties.add(new KeycloakProperty(COUNTRY, accessToken.getAddress().getCountry()));
        properties.add(new KeycloakProperty(FORMATTED_ADDRESS, accessToken.getAddress().getFormattedAddress()));
        properties.add(new KeycloakProperty(LOCALITY, accessToken.getAddress().getLocality()));
        properties.add(new KeycloakProperty(POSTAL_CODE, accessToken.getAddress().getPostalCode()));
        properties.add(new KeycloakProperty(REGION, accessToken.getAddress().getRegion()));
        properties.add(new KeycloakProperty(STREET_ADDRESS, accessToken.getAddress().getStreetAddress()));
      }
    return properties;
  }

  private Collection<? extends Role> createRoles(final AccessToken accessToken) {
    Set<String> roleNames = new HashSet<String>();
    
    //Add app roles first, if any
    AccessToken.Access access = accessToken.getResourceAccess(accessToken.getIssuedFor());
    if(access != null && access.getRoles() != null){
      roleNames.addAll(access.getRoles());
    }

    //Add realm roles next, if any
    AccessToken.Access realmAccess = accessToken.getRealmAccess();
    if(realmAccess != null && realmAccess.getRoles() != null){
      roleNames.addAll(realmAccess.getRoles());
    }

    final List<Role> roles = new ArrayList<Role>(roleNames.size());
    for (final String roleName : roleNames) {
      roles.add(new RoleImpl(roleName));
    }

    return roles;
  }

  /**
   * Set the {@link KeycloakSecurityContext} used to generate the logged in Keycloak {@link User}.
   *
   * @param keycloakSecurityContext The context used to generate the logged in Keycloak {@link User}.
   */
  void setSecurityContext(final KeycloakSecurityContext keycloakSecurityContext) {
    if (wrappedAuthService.isLoggedIn() && keycloakSecurityContext != null) {
      throw new AlreadyLoggedInException("Logged in as " + wrappedAuthService.getUser());
    }    
    this.keycloakSecurityContext = keycloakSecurityContext;
    keycloakUser = null;
  }
}
