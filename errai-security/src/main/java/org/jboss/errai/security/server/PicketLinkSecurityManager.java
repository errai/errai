package org.jboss.errai.security.server;

import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.security.shared.LoggedInEvent;
import org.jboss.errai.security.shared.SecurityManager;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;
import org.picketlink.idm.credential.Password;
import org.jboss.errai.security.shared.User;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Alternative;
import javax.inject.Inject;

/**
 * @author edewit@redhat.com
 */
@Service
@ApplicationScoped
public class PicketLinkSecurityManager implements SecurityManager {

  @Inject
  private Identity identity;

  @Inject
  private Event<LoggedInEvent> loggedInEventSource;

  @Inject
  private DefaultLoginCredentials credentials;

  @Override
  public void login(String username, String password) {
    credentials.setUserId(username);
    credentials.setCredential(new Password(password));

    if (identity.login() != Identity.AuthenticationResult.SUCCESS) {
      throw new SecurityException();
    }

    loggedInEventSource.fire(new LoggedInEvent(createUser(identity.getUser())));
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
  }
}
