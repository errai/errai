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

import org.jboss.errai.common.client.framework.Assert;
import org.jboss.errai.marshalling.client.util.MarshallUtil;

/**
 * @author Mike Brock
 */
public abstract class AbstractMarshallingSession implements MarshallingSession {
  private final MappingContext context;

  private final Map<Object, Integer> objects = new IdentityHashMap<Object, Integer>();
  private final Map<String, Object> objectMap = new HashMap<String, Object>();
  private String assumedElementType = null;
  
  protected AbstractMarshallingSession(MappingContext context) {
    this.context = Assert.notNull(context);
  }

  @Override
  public Marshaller<Object> getMarshallerInstance(String fqcn) {
    Marshaller<Object> marshaller = context.getMarshaller(fqcn);
    if (marshaller == null) {
      if (fqcn.startsWith("[")) {
        String componentClassName = MarshallUtil.getComponentClassName(fqcn);
        marshaller = context.getMarshaller(componentClassName);
        if (marshaller != null) {
          marshaller = new ArrayMarshallerWrapper(marshaller);
        }
      } 
    }
    return marshaller;
  }

  @Override
  public MappingContext getMappingContext() {
    return context;
  }

  @Override
  public boolean hasObject(String hashCode) {
    return objectMap.containsKey(hashCode);
  }

  @Override
  public boolean hasObject(Object reference) {
    return reference != null && objects.containsKey(reference);
  }

  @Override
  public <T> T getObject(Class<T> type, String hashCode) {
    return (T) objectMap.get(hashCode);
  }

  @Override
  public void recordObject(String hashCode, Object instance) {
    if ("-1".equals(hashCode)) return;

    objectMap.put(hashCode, instance);
  }

  @Override
  public String getObject(Object reference) {
    Integer i = objects.get(reference);
    String s;

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
  public void setAssumedElementType(String assumendElementType) {
     this.assumedElementType = assumendElementType;
  }
}
