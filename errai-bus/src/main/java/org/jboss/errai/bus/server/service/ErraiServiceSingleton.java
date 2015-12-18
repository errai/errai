/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public final class ErraiServiceSingleton {
  private ErraiServiceSingleton() {
  }

  private static Set<ErraiInitCallback> callbackList = Collections.synchronizedSet(new HashSet<ErraiInitCallback>());

  private static final Object monitor = new Object();
  private static volatile ErraiService service = new ErraiServiceProxy();

  public static ErraiService initSingleton(final ErraiServiceConfigurator configurator) {
    synchronized (monitor) {
      if (isInitialized()) throw new IllegalStateException("service already set into singleton");
      ErraiServiceProxy proxy = (ErraiServiceProxy) service;
      service = ErraiServiceFactory.create(configurator);
      proxy.closeProxy(service);

      for (ErraiInitCallback erraiInitCallback : callbackList) {
        erraiInitCallback.onInit(service);
      }

      return service;
    }
  }

  public static boolean isInitialized() {
    return !(service instanceof ErraiServiceProxy);
  }

  public static ErraiService getService() {
    return service;
  }

  public static void registerInitCallback(ErraiInitCallback callback) {
    synchronized (monitor) {
      if (isInitialized()) {
        callback.onInit(getService());
      }
      callbackList.add(callback);
    }
  }

  public static Set<ErraiInitCallback> getInitCallbacks() {
    return Collections.unmodifiableSet(callbackList);
  }


  public static interface ErraiInitCallback {
    public void onInit(ErraiService service);
  }
}
