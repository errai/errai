package org.jboss.errai.ioc.rebind.ioc.injector.api;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.injector.Injector;

/**
 * @author Mike Brock
 */
public interface InjectorRegistrationListener {
  public void onRegister(MetaClass type, Injector injector);
}
