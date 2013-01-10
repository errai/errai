/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.security.auth;

import java.util.Collection;
import java.util.Iterator;

/**
 * <tt>AuthSubject</tt> creates an entity that requires authentication. It contains the username, full name of the user,
 * and the roles of the user
 */
public class AuthSubject {
  protected String username;
  protected String fullname;
  protected Collection<Object> roles;

  /**
   * Initializes the subject
   *
   * @param username - the user name of the subject
   * @param fullname - the full name of the subject
   * @param roles    - a collection of roles pertaining to this subject
   */
  public AuthSubject(String username, String fullname, Collection<Object> roles) {
    this.username = username;
    this.fullname = fullname;
    this.roles = roles;
  }

  /**
   * Gets the user name
   *
   * @return the user name
   */
  public String getUsername() {
    return username;
  }

  /**
   * Gets the full name
   *
   * @return the full name
   */
  public String getFullname() {
    return fullname;
  }

  /**
   * Gets the collection of roles
   *
   * @return the roles in a <tt>Collection</tt>
   */
  public Collection<Object> getRoles() {
    return roles;
  }

  /**
   * Convert the collection of roles to a comma-separated string
   *
   * @return the collection of roles represented as a comma-separated string
   */
  public String toRolesString() {
    StringBuilder builder = new StringBuilder();
    Iterator<Object> iter = roles.iterator();
    while (iter.hasNext()) {
      builder.append(String.valueOf(iter.next()));
      if (iter.hasNext()) builder.append(",");
    }

    return builder.toString();
  }
}
