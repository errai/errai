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
import java.util.Map;

/**
 * @author Mike Brock
 */
public abstract class AbstractMarshallingSession implements MarshallingSession {
  private Map<Object, Integer> objects = new HashMap<Object, Integer>();
  private Map<String, Object> objectMap = new HashMap<String, Object>();

  @Override
  public boolean hasObjectHash(String hashCode) {
    return objectMap.containsKey(hashCode);
  }

  @Override
  public boolean hasObjectHash(Object reference) {
    return reference != null && objects.containsKey(reference);
  }

  @Override
  public <T> T getObject(Class<T> type, String hashCode) {
    return (T) objectMap.get(hashCode);
  }

  @Override
  public void recordObjectHash(String hashCode, Object instance) {
    objectMap.put(hashCode, instance);
  }

  @Override
  public String getObjectHash(Object o) {
    Integer i = objects.get(o);
    String s = null;
    
    if (i == null) {
      objects.put(o, (i = objects.size() + 1));
      recordObjectHash(s = i.toString(), o);
    }
    else {
      s = i.toString();
    }

    return s;
  }
}
