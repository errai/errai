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

package org.jboss.errai.ioc.client.container;

/**
 * A simple tuple implementation meant to hold a key-value pair.
 *
* @author Mike Brock
*/
public class Tuple<K, V> {
  private final K key;
  private final V value;

  private Tuple(final K key, final V value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Creates a new Tuple. Either the key or value can be null.
   *
   * @param k the key
   * @param v the value
   * @param <K> the key type
   * @param <V> the value type
   * @return an instance of Tuple.
   */
  public static <K, V> Tuple<K, V> of(final K k, final V v) {
    return new Tuple<K, V>(k, v);
  }

  /**
   * Returns the key. May be null.
   * @return
   */
  public K getKey() {
    return key;
  }

  /**
   * Returns the value. May be null.
   * @return
   */
  public V getValue() {
    return value;
  }

  public String toString() {
    return "[" + getKey() + ", " + getValue() + "]";
  }
}
