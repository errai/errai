/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * <p>
 * Represents a user or other actor which may have permissions to do things within the application.
 *
 * <p>
 * The default implementation within Errai is {@link UserImpl}, but a different implementation may
 * be used so long as it is {@link Portable} and implements all methods of this interface as
 * described by the documentation.
 *
 * @author Max Barkley <mbarkley@redhat.com>
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

  /**
   * @return A unique identifier for this instance.
   */
  String getIdentifier();

  /**
   * The implementation returned must use the {@link Object#equals(Object)} method for comparison.
   *
   * @return The set of all {@link Role Roles} associated with this user.
   */
  Set<Role> getRoles();
  
  /**
   * The implementation returned must use the {@link Object#equals(Object)} method for comparison.
   *
   * @return The set of all {@link Group Groups} associated with this user.
   */
  Set<Group> getGroups();
  
  /**
   * Note: the contents of this map will depend on the implementations of {@link User} and
   * {@link AuthenticationService} being used.
   *
   * @return A map of properties associated with this user.
   */
  Map<String, String> getProperties();

  /**
   * @param name
   *          The name of a property to set.
   * @param value
   *          The value to set. This will override any pre-existing value.
   */
  void setProperty(final String name, final String value);

  /**
   * @param name
   *          The name of a property to remove.
   */
  void removeProperty(final String name);

  /**
   * @param name
   *          The name of a property to get the value of.
   * @return The value of the property, or <code>null</code> if there is no such property in the
   *         {@link #getProperties()} map.
   */
  String getProperty(final String name);

}
