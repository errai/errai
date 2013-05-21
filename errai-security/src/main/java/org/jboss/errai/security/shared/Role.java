package org.jboss.errai.security.shared;

import org.jboss.errai.common.client.api.annotations.Portable;
import org.jboss.errai.marshalling.client.api.annotations.MapsTo;

/**
 * @author edewit@redhat.com
 */
@Portable
public class Role {
  private final String name;

  public Role(@MapsTo("role") String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Role)) return false;

    Role role = (Role) o;
    return name.equals(role.name);
  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
