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

package org.jboss.errai.jpa.client.local;

import javax.persistence.metamodel.EntityType;

/**
 * Errai implementation of the JPA EntityType metamodel interface. Specializes
 * IdentifiableType by adding {@code name} and {@code bindableType} properties.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 *
 * @param <X> The actual entity type described by this metatype.
 */
public abstract class ErraiEntityType<X> extends ErraiIdentifiableType<X> implements EntityType<X> {

  private final String name;

  public ErraiEntityType(String name, Class<X> javaType) {
    super(javaType);
    this.name = name;
  }

  @Override
  public Class<X> getBindableJavaType() {
    return javaType;
  }

  @Override
  public String getName() {
    return name;
  }

  @Override
  public javax.persistence.metamodel.Bindable.BindableType getBindableType() {
    return BindableType.ENTITY_TYPE;
  }

  @Override
  public String toString() {
    return "[EntityType \"" + getName() + "\" (" + getJavaType().getName() + ")]";
  }
}
