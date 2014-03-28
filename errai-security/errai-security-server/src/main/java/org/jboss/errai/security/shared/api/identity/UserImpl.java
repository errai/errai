package org.jboss.errai.security.shared.api.identity;

import static java.util.Collections.*;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.security.shared.api.Role;

@Portable
public class UserImpl implements User, Serializable {

  private static final long serialVersionUID = 3172905561115755369L;

  private final String name;
  private final Set<Role> roles;
  private final Map<String, String> properties = new HashMap<String, String>();

  public UserImpl(final String name) {
    this(name, Collections.<Role> emptyList());
  }

  public UserImpl(final String name, final Collection<? extends Role> roles) {
    this(name, roles, Collections.<String,String> emptyMap());
  }

  public UserImpl(
          @MapsTo("name") final String name,
          @MapsTo("roles") final Collection<? extends Role> roles,
          @MapsTo("properties") final Map<String, String> properties) {
    this.name = name;
    this.roles = Collections.unmodifiableSet(new HashSet<Role>(roles));
    this.properties.putAll(properties);
  }

  @Override
  public Set<Role> getRoles() {
    return roles;
  }

  @Override
  public boolean hasAllRoles(String ... roleNames) {
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

  @Override
  public boolean hasAnyRoles(String ... roleNames) {
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
    return "UserImpl [id=" + name + ", roles=" + roles + ", properties="
            + properties + "]";
  }

}
