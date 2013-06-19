package org.jboss.errai.security.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.*;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.Role;
import org.jboss.errai.security.shared.User;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.IdentityManager;
import org.picketlink.idm.credential.Password;
import org.picketlink.idm.model.*;
import org.picketlink.idm.query.IdentityQuery;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

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
  private IdentityManager identityManager;

  @Inject
  private Event<LoggedInEvent> loggedInEventSource;

  @Inject
  private Event<LoggedOutEvent> loggedOutEventSource;

  @Inject
  private DefaultLoginCredentials credentials;

  @Override
  public void login(String username, String password) {
    credentials.setUserId(username);
    credentials.setCredential(new Password(password));

    if (identity.login() != Identity.AuthenticationResult.SUCCESS) {
      throw new SecurityException();
    }

    loggedInEventSource.fire(new LoggedInEvent(createUser((org.picketlink.idm.model.User) identity.getAgent())));
  }


  /**
   * TODO this is wrong, maybe we can configure the attributes of picketLink to provide us with the right information
   * @param picketLinkUser the user returned by picketLink
   * @return our user
   */
  private User createUser(org.picketlink.idm.model.User picketLinkUser) {
    User user = new User(picketLinkUser.getLoginName());
    user.setFullName(picketLinkUser.getFirstName() + " " + picketLinkUser.getLastName());
    user.setShortName(picketLinkUser.getLastName());
    return user;
  }

  @Override
  public boolean isLoggedIn() {
    return identity.isLoggedIn();
  }

  @Override
  public void logout() {
    identity.logout();
    loggedOutEventSource.fire(new LoggedOutEvent());
  }

  @Override
  public User getUser() {
    if (identity.isLoggedIn()) {
      return createUser((org.picketlink.idm.model.User) identity.getAgent());
    }
    return null;
  }

  @Override
  public List<Role> getRoles() {
    List<Role> roles = new ArrayList<Role>();

    if (identity.isLoggedIn()) {
      IdentityQuery<org.picketlink.idm.model.Role> query =
              identityManager.createIdentityQuery(org.picketlink.idm.model.Role.class);
      query.setParameter(org.picketlink.idm.model.Role.ROLE_OF, identity.getAgent());
      for (org.picketlink.idm.model.Role role : query.getResultList()) {
        roles.add(new Role(role.getName()));
      }
    }

    return roles;
  }
}
