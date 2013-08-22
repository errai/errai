package org.jboss.errai.ioc.support.bus.tests.client.res;

import javax.enterprise.context.Dependent;

import org.jboss.errai.ioc.client.api.AfterInitialization;
import org.jboss.errai.ioc.client.container.IOC;

@Dependent
public class FirstAfterInitBean {

  private boolean afterInitCalled;
  private SecondAfterInitBean bean;

  @AfterInitialization
  public void aferInit() {
    afterInitCalled = true;
    bean = IOC.getBeanManager().lookupBean(SecondAfterInitBean.class).getInstance();
  }

  public boolean isAfterInitCalled() {
    return afterInitCalled;
  }
  
  public SecondAfterInitBean getSecondAfterInitBean() {
    return bean;
  }
}
