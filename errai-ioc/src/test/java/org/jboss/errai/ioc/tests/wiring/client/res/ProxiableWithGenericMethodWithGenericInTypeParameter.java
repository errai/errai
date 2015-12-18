package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProxiableWithGenericMethodWithGenericInTypeParameter {

  public static interface SomeType {

  }

  /**
   * This is a regression test for the following bug:
   * When proxying this method, codegen would resolve the return type to class.
   */
  public <T extends SomeType> T get(Class<T> clazz) {
    return null;
  }

}
