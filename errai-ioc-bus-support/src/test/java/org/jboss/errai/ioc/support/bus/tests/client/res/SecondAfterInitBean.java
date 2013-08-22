package org.jboss.errai.ioc.support.bus.tests.client.res;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.api.AfterInitialization;

@Dependent
public class SecondAfterInitBean {

  private boolean afterInitCalled;

  @AfterInitialization
  public void aferInit() {
    afterInitCalled = true;
  }

  public boolean isAfterInitCalled() {
    return afterInitCalled;
  }
}
