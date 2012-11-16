package org.jboss.errai.ioc.rebind.ioc.injector.async;

import org.jboss.errai.ioc.rebind.ioc.injector.Injector;

/**
 * @author Mike Brock
 */
public interface AsyncInjectorResolveCallback {
  public void onResolved(final Injector resolvedInjector);
}
