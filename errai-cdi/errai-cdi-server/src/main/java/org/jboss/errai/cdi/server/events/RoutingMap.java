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

package org.jboss.errai.cdi.server.events;

import org.jboss.errai.common.client.protocols.MessageParts;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
* @author Mike Brock
*/
public class RoutingMap implements Map<String, Object> {
  private final String queueId;
  private final Map<String, Object> _wrapped;

  RoutingMap(Map<String, Object> _wrapped, String queueId) {
    this._wrapped = _wrapped;
    this.queueId = queueId;
  }

  @Override
  public int size() {
    return _wrapped.size();
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean containsKey(Object key) {
    return MessageParts.SessionID.name().equals(key) || _wrapped.containsKey(key);
  }

  @Override
  public boolean containsValue(Object value) {
    return queueId.equals(value) || _wrapped.containsValue(value);
  }

  @Override
  public Object get(Object key) {
    if (MessageParts.SessionID.name().equals(key)) {
      return queueId;
    }
    else {
      return _wrapped.get(key);
    }
  }

  @Override
  public Object put(String key, Object value) {
    if (MessageParts.SessionID.name().equals(key)) {
      throw new IllegalArgumentException("the key '" + MessageParts.SessionID.name() + "' cannot be updated");
    }
    return _wrapped.put(key, value);
  }

  @Override
  public Object remove(Object key) {
    return _wrapped.remove(key);
  }

  @Override
  public void putAll(Map<? extends String, ? extends Object> m) {
    _wrapped.putAll(m);
  }

  @Override
  public void clear() {
    _wrapped.clear();
  }

  @Override
  public Set<String> keySet() {
    return _wrapped.keySet();
  }

  @Override
  public Collection<Object> values() {
    return _wrapped.values();
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return _wrapped.entrySet();
  }
}
