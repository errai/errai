package org.jboss.errai.ioc.client.container;

/**
 * @author Mike Brock
 */
public class IOCBeanManagerLifecycle {
  public void resetBeanManager() {
    IOC.getBeanManager().destroyAllBeans();
  }
}
