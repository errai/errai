/*
 * Copyright 2012 JBoss Inc
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

package org.jboss.errai.security.shared.api.identity;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.security.shared.api.Role;

/**
 * Represents a user or other actor which may have permissions to do things
 * within the application.
 */
public interface User extends Serializable {

  /**
   * Represents a user who is not logged in. This user has no properties and a
   * {@link Role#NOBODY single role}.
   */
  public static final User ANONYMOUS = new UserImpl("ANONYMOUS", Arrays.asList(Role.NOBODY));

  /**
   * A set of standard property names that most other security identity systems
   * are likely to have information about.
   * 
   * @see User#getProperty(String, String)
   */
  public static class StandardUserProperties {
    public static final String FIRST_NAME = "org.jboss.errai.security.FIRST_NAME";
    public static final String LAST_NAME = "org.jboss.errai.security.LAST_NAME";
    public static final String EMAIL = "org.jboss.errai.security.EMAIL";
  }

  String getIdentifier();

  Set<Role> getRoles();

  boolean hasAllRoles(String... roleNames);

  boolean hasAnyRoles(String... roleNames);

  Map<String, String> getProperties();

  void setProperty(final String name, final String value);

  void removeProperty(final String name);

  String getProperty(final String name);

}