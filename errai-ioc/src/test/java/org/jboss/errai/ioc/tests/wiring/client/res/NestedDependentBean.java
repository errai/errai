package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.annotation.PreDestroy;
import javax.inject.Inject;

public class NestedDependentBean {

  @Inject
  private DependentBean bean;

  private boolean preDestroyCalled;

  public DependentBean getBean() {
    return bean;
  }

  public boolean isPreDestroyCalled() {
    return preDestroyCalled;
  }

  @PreDestroy
  private void preDestroy() {
    preDestroyCalled = true;
  }

}
