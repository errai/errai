package org.jboss.errai.cdi.injection.client;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.New;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see InjectionIntegrationTest}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class InjectionTestModule {

  private boolean postConstructFired;

  @Inject
  private InjectionDependentTestBeanA beanA;

  @Inject
  private InjectionDependentTestBeanC beanC;

  @Inject @New
  private InjectionDependentTestBeanC beacC1;
  
  @PostConstruct
  public void doPostConstruct() {
    postConstructFired = true;
  }

  public InjectionDependentTestBeanA getBeanA() {
    return beanA;
  }

  public InjectionDependentTestBeanC getBeanC() {
    return beanC;
  }

  public InjectionDependentTestBeanC getBeanC1() {
    return beacC1;
  }

  public boolean isPostConstructFired() {
    return postConstructFired;
  }
}