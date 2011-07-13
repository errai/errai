package org.jboss.errai.cdi.client;

import java.util.Random;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see ProducerIntegrationTest}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint
public class ProducerTestModule {
  private static ProducerTestModule instance;

  @Inject
  private ProducerDependentTestBean testBean;

  @Produces
  @A
  private Integer numberA = new Random().nextInt();

  private Integer numberB;

  @Produces
  @B
  public Integer produceNumberB() {
    numberB = new Random().nextInt();
    return numberB;
  }

  @Produces
  public String produceString(@C Integer number) {
    return Integer.toString(number);
  }

  private Integer numberC;

  @Produces
  @C
  public Integer produceNumberC() {
    numberC = 1000;
    return numberC;
  }


  public Integer getNumberA() {
    return numberA;
  }

  public Integer getNumberB() {
    return numberB;
  }

  public Integer getNumberC() {
    return numberC;
  }

  @PostConstruct
  public void doPostConstruct() {
    instance = this;
  }

  public static ProducerTestModule getInstance() {
    return instance;
  }

  public ProducerDependentTestBean getTestBean() {
    return testBean;
  }
}