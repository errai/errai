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

package org.jboss.errai.jpa.client.local.backend;

import java.util.List;

import org.jboss.errai.jpa.client.local.EntityJsonMatcher;
import org.jboss.errai.jpa.client.local.ErraiIdentifiableType;
import org.jboss.errai.jpa.client.local.Key;

/**
 * Represents a browser-local persistent storage backend.
 * <p>
 * WARNING: this interface is in an extreme state of flux. It is guaranteed to
 * change as the first few alternative implementations are developed.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public interface StorageBackend {

  /**
   * Stores the given Errai-Marshalling-Capable value under the given key. A
   * pre-existing value under the same key (if any) is silently replaced by the
   * new one.
   *
   * @param key
   *          The identity of the entry in the storage backend. Null is not
   *          permitted.
   * @param value
   *          The value to store. Must be marshallable using Errai Marshalling.
   *          Null is not permitted; use {@link #remove(Key)} to remove an
   *          entity from this data store.
   * @param <X>
   *          The entity's Java type
   */
  <X> void put(Key<X, ?> key, X value);

  /**
   * Retrieves the value most recently stored in this backend under the given
   * key, reconnecting referenced objects by calling back up into the owning
   * EntityManager.
   * <p>
   * Note that this operation is <i>not</i> a good method for testing if the
   * backend contains a given object, because of the potentially wide-reaching
   * side effects caused by recursively resolving the entity references in the
   * object being retrieved. To test if the backend contains an object for a
   * particular key, use {@link #contains(Key)}.
   *
   * @param key
   *          The identity of the object to be retrieved. The actual entity
   *          returned may be a subtype of the type specified in the key. Null
   *          is not permitted.
   * @param <X>
   *          The entity's Java type
   * @return The retrieved object, reconstituted from its backend (serialized)
   *         representation, including references to other objects. Return value
   *         is null if there is no value presently associated with {@code key}.
   */
  <X> X get(Key<X, ?> key);

  /**
   * Returns all entities of the given type (and its subtypes) whose JSON
   * representations are accepted by the given matcher.
   *
   * @param type
   *          The type of entities to retrieve
   * @param matcher
   *          The matcher that decides which entity instances will be retrieved.
   * @return all matching entities of the given type.
   */
  <X> List<X> getAll(ErraiIdentifiableType<X> type, EntityJsonMatcher matcher);

  /**
   * Tests if this backend contains data for the given key. As with
   * {@link #get(Key)}, subtypes are taken into account. If this backend
   * contains an entity with the same ID as the given key and the same type or a
   * subtype of the type specified in the key, this method will return true.
   *
   * @param key
   *          The identity of the object to be tested for. Null is not
   *          permitted.
   * @return True if the backend contains the entity instance associated with
   *         the given key, and false otherwise.
   */
  <X, Y> boolean contains(Key<X, Y> key);

  /**
   * Removes the key and its associated value (if any) from this storage
   * backend. If the key is not present in this backend, this method returns
   * normally and has no effect.
   *
   * @param key
   *          The identity of the object to be removed. Null is not permitted.
   * @param <X>
   *          The entity's Java type
   */
  <X> void remove(Key<X, ?> key);

  /**
   * Checks if the value currently associated with {@code key} in this backend
   * datastore is identical to the given one. For the purposes of this method,
   * two values are considered identical if their serialized representation is
   * the same. This method does <i>not</i> check for equality using the
   * {@code equals()} method of {@code value}.
   *
   * @param key
   *          The identity of the object to be removed. Null is not permitted.
   * @param value
   *          The value to check. Must be marshallable using Errai Marshalling.
   * @param <X>
   *          The entity's Java type
   */
  <X> boolean isModified(Key<X, ?> key, X value);

  /**
   * Removes all data from this storage backend.
   */
  void removeAll();

}
