/*
 * Copyright 2013 JBoss, by Red Hat, Inc
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

package org.jboss.errai.codegen.meta;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jboss.errai.common.rebind.CacheStore;

/**
 * @author Mike Brock
 */
public class MetaClassCache implements CacheStore {
  private final Map<String, MetaClass> PRIMARY_CLASS_CACHE
      = new ConcurrentHashMap<String, MetaClass>(2000);

  final Map<String, MetaClass> ERASED_CLASS_CACHE
      = new ConcurrentHashMap<String, MetaClass>(2000);

  @Override
  public void clear() {
    PRIMARY_CLASS_CACHE.clear();
    ERASED_CLASS_CACHE.clear();
  }

  public void pushCacheAll(Map<String, MetaClass> mapToPush) {
    PRIMARY_CLASS_CACHE.putAll(mapToPush);
  }

  public void pushCache(final MetaClass clazz) {
    PRIMARY_CLASS_CACHE.put(clazz.getFullyQualifiedName(), clazz);
  }

  public void pushCache(final String fqcn, final MetaClass clazz) {
    PRIMARY_CLASS_CACHE.put(fqcn, clazz);
  }

  public MetaClass get(String fqcn) {
    return PRIMARY_CLASS_CACHE.get(fqcn);
  }

  public Collection<MetaClass> getAllCached() {
    return PRIMARY_CLASS_CACHE.values();
  }

  public int size() {
    return PRIMARY_CLASS_CACHE.size();
  }
  
  public boolean isKnownType(String fqcn) {
    return PRIMARY_CLASS_CACHE.containsKey(fqcn);
  }
}
