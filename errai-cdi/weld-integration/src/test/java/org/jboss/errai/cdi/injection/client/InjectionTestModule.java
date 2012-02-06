package org.jboss.errai.cdi.injection.client;

import javax.annotation.PostConstruct;
import javax.inject.Inject;

import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see InjectionIntegrationTest}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class InjectionTestModule {
  private static InjectionTestModule instance;

  @Inject
  private InjectionDependentTestBeanA beanA;

  @Inject
  private InjectionDependentTestBeanC beanC;
  
  @PostConstruct
  public void doPostConstruct() {
    instance = this;
  }

  public static InjectionTestModule getInstance() {
    return instance;
  }

  public InjectionDependentTestBeanA getBeanA() {
    return beanA;
  }

  public InjectionDependentTestBeanC getBeanC() {
    return beanC;
  }
}