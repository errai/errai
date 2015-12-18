/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.enterprise.client.jaxrs.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

/**
 * GWT-translatable implementation of {@link MultivaluedMap}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class MultivaluedMapImpl<K, V> implements MultivaluedMap<K, V> {
  Map<K, List<V>> map = new HashMap<K, List<V>>();

  @Override
  public void clear() {
    map.clear();
  }

  @Override
  public boolean containsKey(Object key) {
    return map.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return map.containsValue(value);
  }

  @Override
  public Set<Map.Entry<K, List<V>>> entrySet() {
    return map.entrySet();
  }

  @Override
  public List<V> get(Object key) {
    return map.get(key);
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public Set<K> keySet() {
    return map.keySet();
  }

  @Override
  public List<V> put(K key, List<V> value) {
    return map.put(key, value);
  }

  @Override
  public void putAll(Map<? extends K, ? extends List<V>> values) {
    map.putAll(values);
  }

  @Override
  public List<V> remove(Object key) {
    return map.remove(key);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public Collection<List<V>> values() {
    return map.values();
  }

  @Override
  public void putSingle(K key, V value) {
    List<V> values = new ArrayList<V>();
    values.add(value);
    map.put(key, values);
  }

  @Override
  public void add(K key, V value) {
    List<V> list = get(key);
    if (list == null) {
      list = new ArrayList<V>();
    }
    list.add(value);
    put(key, list);
  }

  @Override
  public V getFirst(K key) {
    List<V> values = get(key);
    if (values != null && !values.isEmpty()) {
      return values.get(0);
    }
    return null;
  }
}
