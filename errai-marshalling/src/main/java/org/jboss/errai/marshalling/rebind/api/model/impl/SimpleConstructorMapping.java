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
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.codegen.framework.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.marshalling.rebind.api.model.ConstructorMapping;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Mike Brock
 */
public class SimpleConstructorMapping implements ConstructorMapping {
  private MetaClass toMap;

  private Map<Integer, String> parmsToIndexMap = new HashMap<Integer, String>();
  private Map<Integer, MetaClass> indexToType = new TreeMap<Integer, MetaClass>();

  protected MetaConstructor constructor;

  public void mapParmToIndex(String parm, int index, Class<?> type) {
    mapParmToIndex(parm, index, toMap = JavaReflectionClass.newUncachedInstance(type));
  }


  public void mapParmToIndex(String parm, int index, MetaClass type) {
    parmsToIndexMap.put(index, parm);
    indexToType.put(index, type);
  }

  public SimpleConstructorMapping getCopyForInheritance() {
    SimpleConstructorMapping mapping = new SimpleConstructorMapping();
    mapping.toMap = toMap;
    mapping.parmsToIndexMap = Collections.unmodifiableMap(parmsToIndexMap);
    mapping.indexToType = Collections.unmodifiableMap(indexToType);

    return mapping;
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

  public void setConstructor(MetaConstructor constructor) {
    this.constructor = constructor;
  }

  @Override
  public MetaConstructor getMember() {
    return constructor;
  }

  public void setMappingClass(MetaClass toMap) {
    this.toMap = toMap;

    /**
     * Initialize the default no-arg constructor if it exists.
     */
    if (constructor == null) {
      constructor = toMap.getBestMatchingConstructor(getSignature());
    }
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

  @Override
  public boolean isNoConstruct() {
    return false;
  }
}
