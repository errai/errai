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

import java.util.Set;

import javax.persistence.metamodel.EmbeddableType;
import javax.persistence.metamodel.EntityType;
import javax.persistence.metamodel.ManagedType;
import javax.persistence.metamodel.Metamodel;

import com.google.common.collect.ImmutableBiMap;
import com.google.common.collect.ImmutableBiMap.Builder;

/**
 * Trivial implementation of the JPA Metamodel class. This class is normally
 * instantiated and populated by generated code in ErraiEntityManager.
 * <p>
 * Instances of this class have two distinct lifecycle phases: when first
 * constructed, the package-private methods {@link #addEntityType(EntityType)}
 * and friends may be called to add new entity types. This is normally done from
 * generated code, but test code can also do this manually. Once
 * {@link #freeze()} has been called, the instance is "frozen." When frozen, all
 * the Metamodel interface methods operate properly, but the addXXX() methods
 * throw exceptions when called.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class ErraiMetamodel implements Metamodel {

  // these are populated by a call to freeze()
  private ImmutableBiMap<String, EntityType<?>> entityTypes;
  private ImmutableBiMap<Class<?>, ManagedType<?>> managedTypes;
  private ImmutableBiMap<Class<?>, EmbeddableType<?>> embeddableTypes;

  // these are set to null when freeze() is called
  private Builder<String, EntityType<?>> entityTypeBuilder = ImmutableBiMap.builder();
  private Builder<Class<?>, ManagedType<?>> managedTypeBuilder = ImmutableBiMap.builder();
  private Builder<Class<?>, EmbeddableType<?>> embeddableTypeBuilder = ImmutableBiMap.builder();

  <X> void addEntityType(EntityType<X> e) {
    entityTypeBuilder.put(e.getJavaType().getName(), e);
    managedTypeBuilder.put(e.getJavaType(), e);
  }

  <X> void addManagedType(ManagedType<X> e) {
    managedTypeBuilder.put(e.getJavaType(), e);
  }

  <X> void addEmbeddableType(EmbeddableType<X> e) {
    embeddableTypeBuilder.put(e.getJavaType(), e);
    managedTypeBuilder.put(e.getJavaType(), e);
  }

  /**
   * Freezes the definition of this metamodel. Once frozen, no more entity
   * metadata can be added, and the collections returned by
   * {@link #getEntities()} and friends are immutable.
   */
  void freeze() {
    entityTypes = entityTypeBuilder.build();
    entityTypeBuilder = null;

    managedTypes = managedTypeBuilder.build();
    managedTypeBuilder = null;

    embeddableTypes = embeddableTypeBuilder.build();
    embeddableTypeBuilder = null;
  }

  /**
   * Returns true iff this instance is frozen. See the class-level documentation
   * for a description of the implications.
   */
  boolean isFrozen() {
    return entityTypeBuilder == null;
  }

  /**
   * Works like {@link #entity(String)} but
   *
   * @param className
   *          The fully-qualified class name of the entity type to retrieve (as
   *          returned by {@code Class.getName()}). Null not permitted.
   * @param failIfNotFound
   *          does not throw an exception if the entity type does not exist.
   * @return the ErraiEntityType associated with the named class, or null if
   *         {@code failIfNotFound} is true and no such entity exists.
   */
  @SuppressWarnings("unchecked")
  <X> ErraiEntityType<X> entity(String className, boolean failIfNotFound) {
    ErraiEntityType<X> et = (ErraiEntityType<X>) entityTypes.get(className);
    if (failIfNotFound && et == null) {
      throw new IllegalArgumentException(className + " is not a known entity type");
    }
    return et;
  }

  /**
   * Retrieves an ErraiEntityType by name rather than class reference.
   *
   * @param className
   *          The fully-qualified class name of the entity type to retrieve (as
   *          returned by {@code Class.getName()}). Null not permitted.
   * @return the ErraiEntityType associated with the named class.
   * @throws IllegalArgumentException
   *           if the given class name is not an known entity type.
   */
  public <X> ErraiEntityType<X> entity(String className) {
    return entity(className, true);
  }

  @Override
  public <X> ErraiEntityType<X> entity(Class<X> cls) {
    return entity(cls.getName());
  }

  @Override
  @SuppressWarnings("unchecked")
  public <X> ManagedType<X> managedType(Class<X> cls) {
    return (ManagedType<X>) managedTypes.get(cls);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <X> EmbeddableType<X> embeddable(Class<X> cls) {
    return (EmbeddableType<X>) embeddableTypes.get(cls);
  }

  @Override
  public Set<ManagedType<?>> getManagedTypes() {
    return managedTypes.values();
  }

  @Override
  public Set<EntityType<?>> getEntities() {
    return entityTypes.values();
  }

  @Override
  public Set<EmbeddableType<?>> getEmbeddables() {
    return embeddableTypes.values();
  }

}
