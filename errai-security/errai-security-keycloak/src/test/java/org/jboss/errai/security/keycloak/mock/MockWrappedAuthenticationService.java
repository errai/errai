package org.jboss.errai.security.keycloak.mock;

import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.shared.service.AuthenticationService;

public class MockWrappedAuthenticationService implements AuthenticationService {

  private User user = User.ANONYMOUS;

  @Override
  public User login(String username, String password) {
    user = new UserImpl(username);

    return user;
  }

  @Override
  public boolean isLoggedIn() {
    return !user.equals(User.ANONYMOUS);
  }

  @Override
  public void logout() {
    user = User.ANONYMOUS;
  }

  @Override
  public User getUser() {
    return user;
  }

}
