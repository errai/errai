package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class InjectionDependentTestBeanC {
  @Inject
  private InjectionDependentTestBeanD beanD;

  public InjectionDependentTestBeanD getBeanD() {
    return beanD;
  }
}