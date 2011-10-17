package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class InjectionDependentTestBeanA {
  @Inject
  private InjectionDependentTestBeanB beanB;

  public InjectionDependentTestBeanB getBeanB() {
    return beanB;
  }
}