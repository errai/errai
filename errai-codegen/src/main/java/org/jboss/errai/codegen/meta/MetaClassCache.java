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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

import org.jboss.errai.common.rebind.CacheStore;

/**
 * @author Mike Brock
 */
public class MetaClassCache implements CacheStore {
  private final Map<String, CacheEntry> PRIMARY_CLASS_CACHE
      = new ConcurrentHashMap<String, CacheEntry>(2000);

  private final Map<String, MetaClass> ERASED_CLASS_CACHE
      = new ConcurrentHashMap<String, MetaClass>(2000);

  private final Set<String> invalidated = new ConcurrentSkipListSet<String>();
  private final Set<String> removed = new ConcurrentSkipListSet<String>();
  private final Map<String, CacheEntry> backupClassCache = new ConcurrentHashMap<String, MetaClassCache.CacheEntry>();

  @Override
  public void clear() {
    backupClassCache.clear();
    backupClassCache.putAll(PRIMARY_CLASS_CACHE);

    invalidated.clear();
    removed.clear();

    PRIMARY_CLASS_CACHE.clear();
    ERASED_CLASS_CACHE.clear();
  }

  public void updateCache(Map<String, MetaClass> mapToPush) {
    addNewOrUpdatedToInvalidated(mapToPush);
    addRemoved();
  }

  private void addRemoved() {
    final Set<String> oldKeys = new HashSet<String>(backupClassCache.keySet());
    oldKeys.removeAll(PRIMARY_CLASS_CACHE.keySet());

    for (final String clazzName : oldKeys) {
      if (backupClassCache.get(clazzName).hashCode != CacheEntry.PLACE_HOLDER)
        removed.add(clazzName);
    }
  }

  private void addNewOrUpdatedToInvalidated(Map<String, MetaClass> mapToPush) {
    for (final Entry<String, MetaClass> entry : mapToPush.entrySet()) {
      final CacheEntry newCacheEntry = createCacheEntry(entry.getValue());
      PRIMARY_CLASS_CACHE.put(entry.getKey(), newCacheEntry);
      final CacheEntry previousCacheEntry = backupClassCache.get(entry.getKey());
      if (previousCacheEntry == null || previousCacheEntry.hashCode != newCacheEntry.hashCode) {
        invalidated.add(entry.getKey());
      }
    }
  }

  public void pushCache(final MetaClass clazz) {
    pushCache(clazz.getFullyQualifiedName(), clazz);
  }

  public void pushCache(final String fqcn, final MetaClass clazz) {
    if (!PRIMARY_CLASS_CACHE.containsKey(fqcn)) {
      PRIMARY_CLASS_CACHE.put(fqcn, new CacheEntry(clazz, CacheEntry.PLACE_HOLDER));
      if (!backupClassCache.containsKey(clazz.getFullyQualifiedName())) {
        invalidated.add(fqcn);
      }
    }
  }

  public MetaClass get(String fqcn) {
    final CacheEntry entry = PRIMARY_CLASS_CACHE.get(fqcn);
    return (entry != null) ? entry.cachedClass : null;
  }

  public Collection<MetaClass> getAllCached() {
    final Collection<MetaClass> allCached = new ArrayList<MetaClass>(PRIMARY_CLASS_CACHE.size());
    for (final CacheEntry cacheEntry : PRIMARY_CLASS_CACHE.values()) {
      if (cacheEntry != null) {
        allCached.add(cacheEntry.cachedClass);
      }
    }

    return allCached;
  }

  public Collection<MetaClass> getAllNewOrUpdated() {
    final Collection<MetaClass> newOrUpdated = new ArrayList<MetaClass>(invalidated.size());
    for (final String fqcn : invalidated) {
      newOrUpdated.add(PRIMARY_CLASS_CACHE.get(fqcn).cachedClass);
    }
    return newOrUpdated;
  }

  public Set<String> getAllRemovedClasses() {
    return Collections.unmodifiableSet(removed);
  }

  public boolean isKnownErasedType(final String fqcn) {
    return ERASED_CLASS_CACHE.containsKey(fqcn);
  }

  public MetaClass getErased(final String fqcn) {
    return ERASED_CLASS_CACHE.get(fqcn);
  }

  public void pushErasedCache(final String fqcn, final MetaClass clazz) {
    ERASED_CLASS_CACHE.put(fqcn, clazz);
  }

  public int size() {
    return PRIMARY_CLASS_CACHE.size();
  }

  public boolean isKnownType(String fqcn) {
    return PRIMARY_CLASS_CACHE.containsKey(fqcn);
  }
  
  public boolean isNewOrUpdated(String fqcn) {
    return invalidated.contains(fqcn);
  }

  private class CacheEntry {
    final MetaClass cachedClass;
    final int hashCode;

    static final int PLACE_HOLDER = 0;

    CacheEntry(final MetaClass cachedClass, final int hashCode) {
      this.cachedClass = cachedClass;
      this.hashCode = hashCode;
    }
  }

  private CacheEntry createCacheEntry(final MetaClass cachedClass) {
    return new CacheEntry(cachedClass, cachedClass.hashContent());
  }

}
