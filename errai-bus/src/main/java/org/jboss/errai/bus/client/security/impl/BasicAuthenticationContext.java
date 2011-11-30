/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client.security.impl;

import org.jboss.errai.bus.client.security.AuthenticationContext;
import org.jboss.errai.bus.client.security.Role;

import java.util.Set;

/**
 * This class implements a basic form of the <tt>AuthenticationContext</tt>, nothing fancy is done here.
 */
public class BasicAuthenticationContext implements AuthenticationContext {
  private Set<Role> roles;
  private String name;
  private boolean valid;

  /**
   * Sets the roles corresponding to the name
   *
   * @param roles - the roles that correspond to <tt>name</tt>
   * @param name  - the name of the person
   */
  public BasicAuthenticationContext(Set<Role> roles, String name) {
    this.roles = roles;
    this.name = name;
    valid = true;
  }

  /**
   * Gets the roles for this instance
   *
   * @return the roles
   */
  public Set<Role> getRoles() {
    return roles;
  }

  /**
   * Gets the name for this instance
   *
   * @return the name
   */
  public String getName() {
    return name;
  }

  /**
   * Sets the name
   *
   * @param name - the new name
   */
  public void setName(String name) {
    this.name = name;
  }

  /**
   * Logs current instance out
   */
  public void logout() {
    valid = false;
  }

  /**
   * Returns validity, if logged in
   *
   * @return true if logged in
   */
  public boolean isValid() {
    return valid;
  }
}
