package org.jboss.errai.ioc.rebind.ioc.injector.api;

/**
 * A <tt>RenderingHook</tt> is a listener interface which is used to react to when an injector has finished rendering
 * its code into the bootstrapper. Thus, this provides a way of dealing with potential forward-lookup problems by
 * ensuring that code is rendered only after specific injectors have been rendered.
 * <p>
 * <tt>RenderingHook</tt>s are registered with {@link org.jboss.errai.ioc.rebind.ioc.injector.Injector#addRenderingHook(RenderingHook)}
 *
 * @author Mike Brock
 */
public interface RenderingHook {
  public void onRender(InjectableInstance instance);
}
