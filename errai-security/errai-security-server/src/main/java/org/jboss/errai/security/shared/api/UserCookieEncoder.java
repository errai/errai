package org.jboss.errai.security.shared.api;

import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.security.shared.api.identity.User;

/**
 * Encodes and decodes {@link User} objects persisted in Errai Security cookies.
 */
public class UserCookieEncoder {

  /**
   * The cookie name for the Errai Security cookie.
   */
  public static final String USER_COOKIE_NAME = "errai-active-user";

  /**
   * Encode a cookie value used for persisting a {@link User}.
   * 
   * @param user
   *          The user to be persisted.
   * @return A marhsalled {@link User} that can be decoded by
   *         {@link UserCookieEncoder#fromCookieValue(String)}.
   * 
   * @see User
   * @see UserCookieEncoder#USER_COOKIE_NAME
   * @see UserCookieEncoder#fromCookieValue(String)
   */
  public static String toCookieValue(User user) {
    return Marshalling.toJSON(user);
  }

  /**
   * Decode a persisted {@link User} from a cookie value.
   * 
   * @param userString
   *          A cookie value that has been persisted using
   *          {@link UserCookieEncoder#toCookieValue(User)}.
   * @return The {@link User} object persisted in the given cookie value.
   * 
   * @see User
   * @see UserCookieEncoder#USER_COOKIE_NAME
   * @see UserCookieEncoder#toCookieValue(User)
   */
  public static User fromCookieValue(String userString) {
    return (User) Marshalling.fromJSON(userString);
  }

}
