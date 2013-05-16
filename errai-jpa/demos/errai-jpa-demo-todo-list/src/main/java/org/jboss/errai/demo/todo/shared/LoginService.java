package org.jboss.errai.demo.todo.shared;

import org.jboss.errai.bus.server.annotations.Remote;

@Remote
public interface LoginService {

  /**
   * Attempts to sign in as an existing user in the system. If successful, the
   * given user will be signed in for the current HTTP session.
   *
   * @param userId
   *          The unique username to authenticate as
   * @param password
   *          The plaintext password of the user to authenticate as
   * @return a copy of the newly registered User with its ID filled in.
   * @throws AuthenticationException
   *           if the given credentials are not valid
   */
  User logIn(String userId, String password) throws AuthenticationException;

  /**
   * Returns the currently authenticated user. Returns null if not logged in.
   */
  User whoAmI();

  /**
   * Signs the current user out of the system. Has no effect if not already logged in.
   */
  void logOut();
}
