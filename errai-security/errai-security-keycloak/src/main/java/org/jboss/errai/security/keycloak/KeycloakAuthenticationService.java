package org.jboss.errai.security.keycloak;

import static org.jboss.errai.security.keycloak.properties.KeycloakPropertyNames.ADDRESS;
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
import java.util.List;
import java.util.Set;

import javax.enterprise.context.SessionScoped;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.keycloak.extension.Wrapped;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.User.StandardUserProperties;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.exception.AlreadyLoggedInException;
import org.jboss.errai.security.shared.exception.FailedAuthenticationException;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.keycloak.KeycloakSecurityContext;
import org.keycloak.representations.AccessToken;

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
  @Wrapped
  private Instance<AuthenticationService> authServiceInstance;

  private User keycloakUser;

  private KeycloakSecurityContext keycloakSecurityContext;

  @Override
  public User login(final String username, final String password) {
    if (!keycloakIsLoggedIn()) {
      return performLoginWithWrappedService(username, password);
    }
    else {
      throw new AlreadyLoggedInException("Already logged in through Keycloak.");
    }
  }

  private User performLoginWithWrappedService(final String username, final String password) {
    if (!authServiceInstance.isUnsatisfied()) {
      return authServiceInstance.get().login(username, password);
    }
    else {
      throw new FailedAuthenticationException(
              "Must provide a non-keycloak AuthenticationService to use the login method.");
    }
  }

  @Override
  public boolean isLoggedIn() {
    return keycloakIsLoggedIn() || wrappedIsLoggedIn();
  }

  private boolean wrappedIsLoggedIn() {
    return !authServiceInstance.isUnsatisfied() && authServiceInstance.get().isLoggedIn();
  }

  private boolean keycloakIsLoggedIn() {
    return keycloakSecurityContext != null && keycloakSecurityContext.getToken() != null;
  }

  @Override
  public void logout() {
    if (keycloakIsLoggedIn()) {
      keycloakLogout();
    }
    else if (wrappedIsLoggedIn()) {
      authServiceInstance.get().logout();
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
    else if (wrappedIsLoggedIn()) {
      return authServiceInstance.get().getUser();
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
    final User user = new UserImpl(accessToken.getId(), createRoles(accessToken
            .getRealmAccess().getRoles()));

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
    properties.add(new KeycloakProperty(ADDRESS, accessToken.getAddress()));
    properties.add(new KeycloakProperty(AUDIENCE, accessToken.getAudience()));
    properties.add(new KeycloakProperty(BIRTHDATE, accessToken.getBirthdate()));
    properties.add(new KeycloakProperty(COUNTRY, accessToken.getCountry()));
    properties.add(new KeycloakProperty(FORMATTED_ADDRESS, accessToken.getFormattedAddress()));
    properties.add(new KeycloakProperty(GENDER, accessToken.getGender()));
    properties.add(new KeycloakProperty(LOCALE, accessToken.getLocale()));
    properties.add(new KeycloakProperty(LOCALITY, accessToken.getLocality()));
    properties.add(new KeycloakProperty(MIDDLE_NAME, accessToken.getMiddleName()));
    properties.add(new KeycloakProperty(NAME, accessToken.getName()));
    properties.add(new KeycloakProperty(NICKNAME, accessToken.getNickName()));
    properties.add(new KeycloakProperty(PHONENUMBER, accessToken.getPhoneNumber()));
    properties.add(new KeycloakProperty(PICTURE, accessToken.getPicture()));
    properties.add(new KeycloakProperty(POSTAL_CODE, accessToken.getPostalCode()));
    properties.add(new KeycloakProperty(PREFERRED_USERNAME, accessToken.getPreferredUsername()));
    properties.add(new KeycloakProperty(PROFILE, accessToken.getProfile()));
    properties.add(new KeycloakProperty(REGION, accessToken.getRegion()));
    properties.add(new KeycloakProperty(STREET_ADDRESS, accessToken.getStreetAddress()));
    properties.add(new KeycloakProperty(SUBJECT, accessToken.getSubject()));
    properties.add(new KeycloakProperty(WEBSITE, accessToken.getWebsite()));
    properties.add(new KeycloakProperty(ZONE_INFO, accessToken.getZoneinfo()));
    properties.add(new KeycloakProperty(EMAIL_VERIFIED, String.valueOf(accessToken.getEmailVerified())));
    properties.add(new KeycloakProperty(PHONENUMBER_VERIFIED, String.valueOf(accessToken.getPhoneNumberVerified())));

    return properties;
  }

  private Collection<? extends Role> createRoles(final Set<String> roleNames) {
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
    if (wrappedIsLoggedIn() && keycloakSecurityContext != null) {
      throw new AlreadyLoggedInException("Logged in as " + authServiceInstance.get().getUser());
    }
    this.keycloakSecurityContext = keycloakSecurityContext;
    keycloakUser = null;
  }
}
