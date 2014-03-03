package org.jboss.errai.security.shared;

import org.jboss.errai.bus.server.annotations.Remote;
import org.jboss.errai.security.shared.exception.AuthenticationException;

/**
 * AuthenticationService service for authenticating users and getting their roles.
 *
 * @author edewit@redhat.com
 */
@Remote
public interface AuthenticationService {

  /**
   * Login with the given username and password.
   * 
   * @param username The username to log in with.
   * @param password The password to authenticate with.
   * @return The logged in {@link User}.
   * @throws Implementations should throw an {@link AuthenticationException} if authentication fails.
   */
  public User login(String username, String password);

  /**
   * @return True iff the user is currently logged in.
   */
  public boolean isLoggedIn();

  /**
   * Log out the currently authenticated user.
   */
  public void logout();

  /**
   * Get the currently authenitcated user.
   * 
   * @return The currently authenticated user, or {@code null} if no user is authenticated.
   */
  public User getUser();
}
