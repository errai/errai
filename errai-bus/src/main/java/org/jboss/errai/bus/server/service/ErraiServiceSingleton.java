package org.jboss.errai.bus.server.service;

/**
 * @author Mike Brock
 */
public final class ErraiServiceSingleton {
  private ErraiServiceSingleton() {
  }

  private static ErraiServiceProxy preReferenceProxy;
  private static ErraiService service;

  public static ErraiService initSingleton(ErraiServiceConfigurator configurator) {
    if (service != null) throw new IllegalStateException("service already set into singleton");
    service = ErraiServiceFactory.create(configurator);
    if (preReferenceProxy != null) {
      preReferenceProxy.closeProxy(service);
    }
    return service;
  }

  public static boolean isInitialized() {
    return service != null;
  }

  public static ErraiService getService() {
    if (service == null) {
      if (preReferenceProxy == null) {
        preReferenceProxy = new ErraiServiceProxy();
      }
      return preReferenceProxy;
    }
    return service;
  }
}
