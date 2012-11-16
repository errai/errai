package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Inject;

import org.jboss.errai.common.client.api.Assert;

public class ConstructorInjectedBean {

  @Inject
  public ConstructorInjectedBean(FooService fs) {
    Assert.notNull(fs);
  }
}
