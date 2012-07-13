/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.marshalling.client.api;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public abstract class AbstractMarshallingSession implements MarshallingSession {
  private final Map<Object, Integer> objects = new IdentityHashMap<Object, Integer>();
  private final Map<String, Object> objectMap = new HashMap<String, Object>();
  private String assumedElementType = null;

  @Override
  public boolean hasObject(final String hashCode) {
    return objectMap.containsKey(hashCode);
  }

  @Override
  public boolean hasObject(final Object reference) {
    return reference != null && objects.containsKey(reference);
  }

  @SuppressWarnings("unchecked")
  @Override
  public <T> T getObject(final Class<T> type, final String hashCode) {
    return (T) objectMap.get(hashCode);
  }

  @Override
  public void recordObject(final String hashCode, final Object instance) {
    if ("-1".equals(hashCode)) return;

    objectMap.put(hashCode, instance);
  }

  @Override
  public String getObject(final Object reference) {
    Integer i = objects.get(reference);
    final String s;
    
    if (i == null) {
      objects.put(reference, (i = objects.size() + 1));
      recordObject(s = i.toString(), reference);
    }
    else {
      s = i.toString();
    }

    return s;
  }

  @Override
  public String getAssumedElementType() {
    return this.assumedElementType;
  }

  @Override
  public void setAssumedElementType(final String assumendElementType) {
     this.assumedElementType = assumendElementType;
  }
}