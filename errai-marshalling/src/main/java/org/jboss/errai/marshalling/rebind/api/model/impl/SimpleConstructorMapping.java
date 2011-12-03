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

import org.jboss.errai.codegen.framework.Statement;
import org.jboss.errai.codegen.framework.meta.MetaClass;
import org.jboss.errai.codegen.framework.meta.MetaClassFactory;
import org.jboss.errai.codegen.framework.meta.MetaConstructor;
import org.jboss.errai.marshalling.rebind.api.model.ConstructorMapping;
import org.jboss.errai.marshalling.rebind.api.model.Mapping;

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


  public void mapParmToIndex(String parm, int index, Class<?> type) {
    mapParmToIndex(parm, index, MetaClassFactory.get(type));
  }

  public void mapParmToIndex(String parm, int index, MetaClass type) {
    parmsToIndexMap.put(index, parm);
    indexToType.put(index, type.asBoxed());
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
  public Class<?>[] getConstructorSignature() {
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

  private MetaConstructor _constructorCache;

  @Override
  public MetaConstructor getConstructor() {
    if (_constructorCache != null) {
      return _constructorCache;
    }

    return _constructorCache = toMap.getConstructor(getConstructorSignature());
  }

  public void setMappingClass(MetaClass toMap) {
    this.toMap = toMap;
  }

  private static class SimpleMapping extends AbstractMapping {
    private String key;
    private MetaClass type;

    private SimpleMapping(String key, MetaClass type) {
      this.key = key;
      this.type = type.asBoxed();
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
