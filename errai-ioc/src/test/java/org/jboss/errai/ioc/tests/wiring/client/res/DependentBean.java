package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.annotation.PreDestroy;
import javax.enterprise.context.Dependent;

/**
 * @author Mike Brock
 */
@Dependent
public class DependentBean {
  private boolean preDestroyCalled = false;

  @PreDestroy
  private void preDestroy() {
    preDestroyCalled = true;
  }

  public boolean isPreDestroyCalled() {
    return preDestroyCalled;
  }
}
