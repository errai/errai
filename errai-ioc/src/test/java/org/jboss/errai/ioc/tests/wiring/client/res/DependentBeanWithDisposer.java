package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.Disposer;

/**
 * @author Mike Brock
 */
@Dependent
public class DependentBeanWithDisposer {
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
