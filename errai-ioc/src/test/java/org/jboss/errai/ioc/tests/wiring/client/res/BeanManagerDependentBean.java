package org.jboss.errai.ioc.tests.wiring.client.res;

import com.google.gwt.core.client.SingleJsoImpl;
import org.jboss.errai.ioc.client.container.IOCBeanManager;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class BeanManagerDependentBean {
  @Inject IOCBeanManager beanManager;

  public IOCBeanManager getBeanManager() {
    return beanManager;
  }
}
