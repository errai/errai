package org.jboss.errai.enterprise.jaxrs.client.shared.entity;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.common.client.api.annotations.MapsTo;
import org.jboss.errai.common.client.api.annotations.NonPortable;
import org.jboss.errai.common.client.api.annotations.Portable;

@Portable
public class ImmutableEntity {
  @NonPortable
  public static class Builder {
    @SuppressWarnings("serial")
    private final List<Entity> entities = new ArrayList<Entity>() {
      {
        add(new Entity(1, "name1"));
        add(new Entity(2, "name2"));
      }
    };

    public ImmutableEntity build() {
      return new ImmutableEntity(this);
    }
  }

  private final List<Entity> entities;

  protected ImmutableEntity(final Builder builder) {
    this(builder.entities);
  }

  protected ImmutableEntity(@MapsTo("entities") final List<Entity> entities) {
    this.entities = entities;
  }

  public List<Entity> getTranslations() {
    return entities;
  }

  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((entities == null) ? 0 : entities.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ImmutableEntity other = (ImmutableEntity) obj;
    if (entities == null) {
      if (other.entities != null)
        return false;
    }
    else if (!entities.equals(other.entities))
      return false;
    return true;
  }

  @Override
  public String toString() {
    return "ImmutableEntity [entities=" + entities + "]";
  }
}