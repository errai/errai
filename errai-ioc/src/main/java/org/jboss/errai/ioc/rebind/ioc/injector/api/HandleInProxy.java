package org.jboss.errai.ioc.rebind.ioc.injector.api;

import org.jboss.errai.codegen.Context;
import org.jboss.errai.codegen.Statement;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.ioc.rebind.ioc.injector.ProxyInjector;

/**
* @author Mike Brock
*/
public class HandleInProxy implements Statement {
  private final ProxyInjector proxyInjector;
  private final Statement wrapped;

  public HandleInProxy(ProxyInjector proxyInjector1, Statement wrapped) {
    this.proxyInjector = proxyInjector1;
    this.wrapped = wrapped;
  }

  public ProxyInjector getProxyInjector() {
    return proxyInjector;
  }

  @Override
  public String generate(Context context) {
    return wrapped.generate(context);
  }

  @Override
  public MetaClass getType() {
    return wrapped.getType();
  }
}
