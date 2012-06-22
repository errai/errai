package org.jboss.errai.bus.server.service;

/**
 * @author Mike Brock
 */
public final class ErraiServiceSingleton {
  private ErraiServiceSingleton() {
  }

  private static final Object monitor = new Object();
  private static volatile ErraiService service = new ErraiServiceProxy();

  public static ErraiService initSingleton(ErraiServiceConfigurator configurator) {
    synchronized (monitor) {
      if (isInitialized()) throw new IllegalStateException("service already set into singleton");
      ErraiServiceProxy proxy = (ErraiServiceProxy) service;
      service = ErraiServiceFactory.create(configurator);
      proxy.closeProxy(service);
      return service;
    }
  }

  public static boolean isInitialized() {
    return !(service instanceof ErraiServiceProxy);
  }

  public static ErraiService getService() {
    return service;
  }
}
