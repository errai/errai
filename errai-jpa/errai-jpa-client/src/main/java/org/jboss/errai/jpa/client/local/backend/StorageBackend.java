package org.jboss.errai.jpa.client.local.backend;

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
   * key.
   *
   * @param key
   *          The identity of the object to be retrieved. Null is not permitted.
   * @param <X>
   *          The entity's Java type
   * @return The retrieved object, reconstituted from its backend (serialized)
   *         representation using Errai Marshalling. Return value is null if
   *         there is no value presently associated with {@code key}.
   */
  <X> X get(Key<X, ?> key);

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

}
