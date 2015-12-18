package org.jboss.errai.ioc.tests.wiring.client.res;

import org.jboss.errai.ioc.client.api.Disposer;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author Mike Brock
 */
@Singleton
public class SingletonBeanWithDisposer {
  @Inject
  Disposer<DependentBean> dependentBeanDisposer;

  @Inject
  DependentBean bean;

  public void dispose() {
    dependentBeanDisposer.dispose(bean);
  }

  public Disposer<DependentBean> getDependentBeanDisposer() {
    return dependentBeanDisposer;
  }

  public DependentBean getBean() {
    return bean;
  }
}
