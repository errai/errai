/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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
