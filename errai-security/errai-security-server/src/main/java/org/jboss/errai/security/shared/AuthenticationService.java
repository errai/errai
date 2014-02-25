package org.jboss.errai.security.shared;

import org.jboss.errai.bus.server.annotations.Remote;

/**
 * AuthenticationService service for authenticating users and get there roles.
 *
 * @author edewit@redhat.com
 */
@Remote
public interface AuthenticationService {

  public User login(String username, String password);

  public boolean isLoggedIn();

  public void logout();

  public User getUser();
}
