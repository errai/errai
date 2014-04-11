package org.jboss.errai.security.shared.api;

import org.jboss.errai.marshalling.client.Marshalling;
import org.jboss.errai.security.shared.api.identity.User;

public class UserCookieEncoder {

  public static final String USER_COOKIE_NAME = "errai-active-user";

  public static String toCookieValue(User user) {
    return Marshalling.toJSON(user);
  }

  public static User fromCookieValue(String userString) {
    return (User) Marshalling.fromJSON(userString);
  }

}
