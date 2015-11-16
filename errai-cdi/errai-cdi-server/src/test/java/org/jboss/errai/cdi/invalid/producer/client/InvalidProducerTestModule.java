package org.jboss.errai.cdi.invalid.producer.client;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see ProducerIntegrationTest}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class InvalidProducerTestModule {
  private static InvalidProducerTestModule instance;
  
  @Inject
  private InvalidProducerDependentTestBean testBean;

  @Produces @A
  private Integer numberA = 1;

  public Integer getNumberA() {
    return numberA;
  }
  
  @PostConstruct
  public void doPostConstruct() {
    instance = this;
  }

  public static InvalidProducerTestModule getInstance() {
    return instance;
  }

  public InvalidProducerDependentTestBean getTestBean() {
    return testBean;
  }
}