package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class SetterInjectionBean {
  private ServiceA serviceA;
  private ServiceB serviceB;

  public ServiceA getServiceA() {
    return serviceA;
  }

  @Inject
  public void setServiceA(ServiceA serviceA) {
    this.serviceA = serviceA;
  }

  public ServiceB getServiceB() {
    return serviceB;
  }

  @Inject
  private void setServiceB(ServiceB serviceB) {
    this.serviceB = serviceB;
  }
}
