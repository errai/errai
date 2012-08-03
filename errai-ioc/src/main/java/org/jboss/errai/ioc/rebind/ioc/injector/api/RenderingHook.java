package org.jboss.errai.ioc.rebind.ioc.injector.api;

/**
 * @author Mike Brock
 */
public interface RenderingHook {
  public void onRender(InjectableInstance instance);
}
