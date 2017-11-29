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
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Mike Brock
 */
public final class ErraiServiceSingleton {
  private ErraiServiceSingleton() {
  }

  private static Set<InitCallbackBlock> callbacks = Collections.synchronizedSet(new HashSet<>());

  private static final Object monitor = new Object();

  private static volatile ErraiService service;
  private static volatile ErraiServiceProxy proxy = new ErraiServiceProxy();

  public static ErraiService initSingleton(final ErraiServiceConfigurator configurator) {
    synchronized (monitor) {
      if (isActive()) throw new IllegalStateException("service already set into singleton");

      service = ErraiServiceFactory.create(configurator);
      proxy.closeProxy(service);

      Iterator<InitCallbackBlock> it = callbacks.iterator();
      while(it.hasNext()) {
        InitCallbackBlock block = it.next();
        block.callback.onInit(service);

        if(!block.persistent) {
          it.remove();
        }
      }
      return service;
    }
  }

  /**
   * Is the {@link ErraiService} still active.
   *
   * @return true if we aren't null and we have a valid {@link ErraiService#getBus()}.
   */
  public static boolean isActive() {
    return service != null && service.getBus() != null;
  }

  /**
   * Get the {@link ErraiService} singleton, or if not yet active provide a valid proxy.
   */
  public static ErraiService getService() {
    return isActive() ? service : proxy;
  }

  /**
   * Reset the proxy and dereference the service.
   */
  public static void resetProxyAndService() {
    proxy.reset();
    service = null;
  }

  /**
   * Register an init callback that will be executed upon initialization.
   *
   * @param callback valid callback.
   */
  public static void registerInitCallback(ErraiInitCallback callback) {
    registerInitCallback(callback, false);
  }

  /**
   * Register an init callback that will be executed upon initialization.
   *
   * @param callback valid callback.
   * @param persistent should the callback persist after the singleton is initialized, will be removed if false.
   */
  public static void registerInitCallback(ErraiInitCallback callback, boolean persistent) {
    synchronized (monitor) {
      InitCallbackBlock block = new InitCallbackBlock(callback, persistent);

      if (isActive()) {
        callback.onInit(getService());

        if(persistent) {
          callbacks.add(block);
        }
      } else {
        callbacks.add(block);
      }
    }
  }

  public static Set<ErraiInitCallback> getInitCallbacks() {
    return Collections.unmodifiableSet(callbacks.stream().map(block -> block.callback).collect(Collectors.toSet()));
  }

  static class InitCallbackBlock {
    ErraiInitCallback callback;
    boolean persistent;

    public InitCallbackBlock(ErraiInitCallback callback, boolean persistent) {
      this.callback = callback;
      this.persistent = persistent;
    }
  }

  public interface ErraiInitCallback {
    void onInit(ErraiService service);
  }
}
