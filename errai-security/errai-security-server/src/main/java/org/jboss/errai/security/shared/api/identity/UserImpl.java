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

package org.jboss.errai.security.shared.api.identity;

import static java.util.Collections.unmodifiableMap;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.security.shared.api.Group;
import org.jboss.errai.security.shared.api.Role;

/**
 * Default implementation of {@link User}, used by Errai's PicketLink and
 * Keycloak integration modules. On the client, Errai should never reference
 * this type directly. The interface should be used instead to provide the
 * ability to plug in custom {@link User} implementations.
 * 
 * Errai, by default, assigns no semantics to groups. Only roles are relevant
 * when checking permissions.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Max Barkley <mbarkley@redhat.com>
 */
@Portable
public class UserImpl implements User, Serializable {

  private static final long serialVersionUID = 3172905561115755369L;

  private final String name;
  private final Set<Role> roles;
  private final Set<Group> groups;
  private final Map<String, String> properties = new HashMap<String, String>();

  public UserImpl(final String name) {
    this(name, Collections.<Role> emptySet());
  }

  public UserImpl(final String name, final Collection<? extends Role> roles) {
    this(name, roles, Collections.<Group> emptySet(), Collections.<String, String> emptyMap());
  }

  public UserImpl(final String name, final Collection<? extends Role> roles, final Map<String, String> properties) {
    this(name, roles, Collections.<Group> emptySet(), properties);
  }

  public UserImpl(final String name, final Collection<? extends Role> roles, final Collection<? extends Group> groups) {
    this(name, roles, groups, Collections.<String, String> emptyMap());
  }

  public UserImpl(
          @MapsTo("name") final String name, 
          @MapsTo("roles") final Collection<? extends Role> roles,
          @MapsTo("groups") final Collection<? extends Group> groups,
          @MapsTo("properties") final Map<String, String> properties) {

    this.name = name;
    this.roles = Collections.unmodifiableSet(new HashSet<Role>(roles));
    this.groups = Collections.unmodifiableSet(new HashSet<Group>(groups));
    this.properties.putAll(properties);
  }

  @Override
  public Set<Role> getRoles() {
    return roles;
  }

  public boolean hasAllRoles(String... roleNames) {
    for (String roleName : roleNames) {
      boolean foundThisOne = false;
      for (Role role : roles) {
        if (roleName.equals(role.getName())) {
          foundThisOne = true;
          break;
        }
      }
      if (!foundThisOne) {
        return false;
      }
    }
    return true;
  }

  public boolean hasAnyRoles(String... roleNames) {
    for (Role role : roles) {
      for (String roleName : roleNames) {
        if (roleName.equals(role.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Set<Group> getGroups() {
    return groups;
  }

  public boolean hasAllGroups(String... groupNames) {
    for (String groupName : groupNames) {
      boolean foundThisOne = false;
      for (Group group : groups) {
        if (groupName.equals(group.getName())) {
          foundThisOne = true;
          break;
        }
      }
      if (!foundThisOne) {
        return false;
      }
    }
    return true;
  }

  public boolean hasAnyGroups(String... groupNames) {
    for (Group group : groups) {
      for (String groupName : groupNames) {
        if (groupName.equals(group.getName())) {
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public Map<String, String> getProperties() {
    return unmodifiableMap(properties);
  }

  @Override
  public void setProperty(final String name, final String value) {
    properties.put(name, value);
  }

  @Override
  public void removeProperty(final String name) {
    properties.remove(name);
  }

  @Override
  public String getProperty(final String name) {
    return properties.get(name);
  }

  @Override
  public String getIdentifier() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof User)) {
      return false;
    }

    User user = (User) o;

    return name.equals(user.getIdentifier());
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }

  @Override
  public String toString() {
    return "UserImpl [id=" + name + ", roles=" + roles + ", groups=" + groups + ", properties=" + properties + "]";
  }

}
