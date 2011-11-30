/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.marshalling.server;

import java.lang.ref.SoftReference;
import java.util.Map;
import java.util.WeakHashMap;

public class EncodingCache {
  private static final Map<Object, SoftReference<?>> ENC_CACHE = new WeakHashMap<Object, SoftReference<?>>(1000);

  private static void store(Object ref, Object val) {
    ENC_CACHE.put(ref, new SoftReference<Object>(val));
  }

  private static <T> T storeAndGet(Object ref, ValueProvider<T> provider) {
    T v = provider.get();
    store(ref, v);
    return v;
  }

  public static <T> T get(Object ref, ValueProvider<T> provider) {
    SoftReference<T> softRef = (SoftReference<T>) ENC_CACHE.get(ref);
    T val;
    if (softRef != null) {
      if ((val = softRef.get()) != null) return val;
    }
    return storeAndGet(ref, provider);
  }

  public static interface ValueProvider<V> {
    public V get();
  }
}


