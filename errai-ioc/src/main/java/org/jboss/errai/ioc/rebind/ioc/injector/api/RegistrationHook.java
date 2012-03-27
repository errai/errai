package org.jboss.errai.ioc.rebind.ioc.injector.api;

import org.jboss.errai.codegen.Statement;

/**
 * @author Mike Brock
 */
public interface RegistrationHook {
  public void onRegister(InjectionContext context, Statement beanValue);
}
