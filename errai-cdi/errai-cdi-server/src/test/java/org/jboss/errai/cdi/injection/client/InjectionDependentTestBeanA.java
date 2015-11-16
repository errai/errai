package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class InjectionDependentTestBeanA {
  private InjectionDependentTestBeanB beanB;

  // must be proxyable.
  public InjectionDependentTestBeanA() {
  }

  @Inject
  public InjectionDependentTestBeanA(InjectionDependentTestBeanB beanB) {
    this.beanB = beanB;
  }

  public InjectionDependentTestBeanB getBeanB() {
    return beanB;
  }
}