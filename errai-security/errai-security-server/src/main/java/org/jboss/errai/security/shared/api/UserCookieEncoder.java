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
    return (User) Marshalling.fromJSON(unquoteIfNeeded(userString));
  }

  /**
   * Unquotes a cookie value if it has been quoted and escaped by the Jetty web server.
   * <p>
   * This method is based on (originally copied from) the org.mortbay.util.QuotedStringTokenizer.unquote method from
   * Jetty 6.1.25.
   * 
   * @param s
   *          the cookie value that may or may not be quoted.
   * @return an unquoted version of the string, or the given input string if it was not quoted.
   */
  public static String unquoteIfNeeded(String s) {
    if (s == null) {
      return null;
    }
    if (s.length() < 2) {
      return s;
    }

    final char first = s.charAt(0);
    final char last = s.charAt(s.length() - 1);
    if (first != last || (first != '"' && first != '\'')) {
      return s;
    }

    StringBuilder b = new StringBuilder(s.length() - 2);
    boolean escape = false;
    for (int i = 1; i < s.length() - 1; i++) {
      char c = s.charAt(i);

      if (escape) {
        escape = false;
        switch (c) {
        case 'n':
          b.append('\n');
          break;
        case 'r':
          b.append('\r');
          break;
        case 't':
          b.append('\t');
          break;
        case 'f':
          b.append('\f');
          break;
        case 'b':
          b.append('\b');
          break;
        case 'u':
          b.append((char) Integer.parseInt(s.substring(i, i + 4), 16));
          i += 4;
          break;
        default:
          b.append(c);
        }
      }
      else if (c == '\\') {
        escape = true;
        continue;
      }
      else {
        b.append(c);
      }
    }

    return b.toString();
  }

}
