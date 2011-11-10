package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class InjectionDependentTestBeanC {

  private InjectionDependentTestBeanD beanD;
  
  @Inject
  private InjectionDependentTestBeanB beanB;
  
  public InjectionDependentTestBeanC() {}
  
  @Inject
  public InjectionDependentTestBeanC(InjectionDependentTestBeanD beanD) {
    this.beanD = beanD;
  }
  
  public InjectionDependentTestBeanD getBeanD() {
    return beanD;
  }
  
  public InjectionDependentTestBeanB getBeanB() {
    return beanB;
  }
}