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

package org.jboss.errai.marshalling.rebind.api.model.impl;

import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaMethod;
import org.jboss.errai.codegen.framework.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.marshalling.rebind.api.model.FactoryMapping;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Mike Brock
 */
public class SimpleFactoryMapping implements FactoryMapping {
  private MetaClass toMap;

  private Map<Integer, String> parmsToIndexMap = new HashMap<Integer, String>();
  private Map<Integer, MetaClass> indexToType = new TreeMap<Integer, MetaClass>();

  private MetaMethod method;

  public void mapParmToIndex(String parm, int index, Class<?> type) {
    mapParmToIndex(parm, index, JavaReflectionClass.newUncachedInstance(type));
  }

  public void mapParmToIndex(String parm, int index, MetaClass type) {
    parmsToIndexMap.put(index, parm);
    indexToType.put(index, type);
  }

  public MetaClass[] getConstructorParmTypes() {
    return indexToType.values().toArray(new MetaClass[indexToType.size()]);
  }

  public String[] getKeyNames() {
    return parmsToIndexMap.values().toArray(new String[parmsToIndexMap.size()]);
  }

  private Mapping[] _mappingsCache;

  @Override
  public Mapping[] getMappings() {
    if (_mappingsCache != null) {
      return _mappingsCache;
    }
    Mapping[] mappings = new Mapping[indexToType.size()];
    final MetaClass[] types = getConstructorParmTypes();
    final String[] keys = getKeyNames();

    for (int i = 0; i < mappings.length; i++) {
      mappings[i] = new SimpleMapping(keys[i], types[i]);
    }

    return _mappingsCache = mappings;
  }

  private Class<?>[] _constructorSignature;

  @Override
  public Class<?>[] getSignature() {
    if (_constructorSignature != null) {
      return _constructorSignature;
    }

    Mapping[] ms = getMappings();
    Class<?>[] sig = new Class<?>[ms.length];
    int i = 0;
    for (Mapping m : ms) {
      sig[i++] = m.getType().asClass();
    }
    return _constructorSignature = sig;
  }

  public void setMethod(MetaMethod method) {
    this.method = method;
  }

  @Override
  public MetaMethod getMember() {
    return method;
  }

  public void setMappingClass(MetaClass toMap) {
    this.toMap = toMap;
  }

  @Override
  public MetaClass getMappingClass() {
    return toMap;
  }

  private static class SimpleMapping extends AbstractMapping {
    private String key;
    private MetaClass type;

    private SimpleMapping(String key, MetaClass type) {
      this.key = key;
      this.type = type;
    }

    @Override
    public String getKey() {
      return key;
    }

    @Override
    public MetaClass getType() {
      return type;
    }
  }

}
