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
