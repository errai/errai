package org.jboss.errai.ioc.rebind.ioc.injector;

import org.jboss.errai.codegen.builder.BlockBuilder;
import org.jboss.errai.ioc.rebind.ioc.injector.api.InjectionContext;

/**
 * @author Mike Brock
 */
public interface BeanInstantiationCallback {
  public void instantiateBean(InjectionContext injectContext, BlockBuilder creationCallbackMethod);
}
