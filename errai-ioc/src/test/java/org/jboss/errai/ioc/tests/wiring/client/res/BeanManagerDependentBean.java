package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.container.ClientBeanManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class BeanManagerDependentBean {
  @Inject ClientBeanManager beanManager;

  public ClientBeanManager getBeanManager() {
    return beanManager;
  }
}
