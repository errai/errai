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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.WrappedPortable;

/**
 * A container for all the live, in-memory objects in a particular entity manager.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class PersistenceContext {

  private final ErraiMetamodel mm;

  /**
   * Maps the key for an entity in the persistence context to the actual object that the key refers to.
   */
  private final Map<Key<?, ?>, Object> contents = new HashMap<Key<?, ?>, Object>();

  /**
   * Creates a new PersistenceContext that can track entities represented within
   * the given metamodel.
   *
   * @param mm
   *          The metamodel that knows about all the entity types that will be
   *          stored in this Persistence Context. Not null.
   */
  public PersistenceContext(ErraiMetamodel mm) {
    this.mm = Assert.notNull(mm);
  }

  /**
   * Stores the given object in the persistence context. Discovers its correct
   * runtime class and builds the correct Key instance for it.
   *
   * @param key
   *          The key to store the entity under. Care must be taken that this
   *          key type reflects the actual runtime type of the given object.
   * @param object
   *          The object to put into the persistence context.
   */
  public <X, Y> void put(Key<X, Y> key, X object) {
    contents.put(key, object);
  }

  /**
   * Returns a Key instance corresponding to the runtime type of the given
   * object, and the given ID value.
   *
   * @param object
   *          The object to get the type information from. If it is a proxy
   *          (WrappedPortable) it will be unwrapped. Must not be null.
   * @param id
   *          The ID value for the key. Must not be null.
   * @return A key for the given ID and the exact type of the given object.
   */
  @SuppressWarnings("unchecked")
  private <X, Y> Key<X, Y> normalizedKey(X object, Y id) {
    X unwrapped = object;
    if (object instanceof WrappedPortable) {
      unwrapped = (X) ((WrappedPortable) object).unwrap();
    }

    ErraiManagedType<X> actualEntityType = (ErraiManagedType<X>) mm.entity(unwrapped.getClass());
    Key<X, Y> normalizedKey = new Key<X, Y>(actualEntityType, id);
    return normalizedKey;
  }

  /**
   * Removes the entity having the given key from this persistence context.
   *
   * @param key
   *          The key of the entity to remove. The key type must be an exact
   *          match for the target object's runtime type.
   */
  public void remove(Key<?, ?> key) {
    contents.remove(key);
  }

  /**
   * Looks up and returns the entity that matches the given key. The key's type
   * need not be an exact match; any supertype of the requested entity will
   * suffice.
   *
   * @param key
   *          the key to look up. The entity type portion can be any supertype
   *          of the matched entity. The ID is always an exact match. Must not
   *          be null.
   * @return The entity that matches the ID and has the same type or a subtype
   *         of the type specified in the key.
   */
  @SuppressWarnings("unchecked")
  public <X> X get(Key<X, ?> key) {
    for (ErraiManagedType<? extends X> mt : key.getEntityType().getSubtypes()) {
      Key<? super X, ?> k = new Key<Object, Object>((ErraiManagedType<Object>) mt, key.getId());
      X o = (X) contents.get(k);
      if (o != null) {
        return o;
      }
    }
    return null;
  }

  /**
   * Returns true if this persistence context contains an entity retrievable by
   * the given key. The type matching is done in the same way as described in
   * {@link #get(Key)}.
   *
   * @param key
   *          the identity of the entity to look for. Must not be null.
   * @return True if and only if this persistence context contains an entity
   *         retrievable by the given key.
   */
  public boolean contains(Key<?, ?> key) {
    return get(key) != null;
  }

  /**
   * Returns the set of all entities in this persistence context. The type
   * portion of the keys reflect the actual runtime type of the entity.
   */
  public Set<Map.Entry<Key<?, ?>, Object>> entrySet() {
    return contents.entrySet();
  }

  /**
   * Returns the collection of all entities in this persistence context.
   */
  public Collection<Object> values() {
    return contents.values();
  }
}
