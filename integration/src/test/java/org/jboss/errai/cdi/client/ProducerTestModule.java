package org.jboss.errai.cdi.client;

import java.util.Random;

import javax.annotation.PostConstruct;
import javax.enterprise.inject.Produces;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.ioc.client.api.EntryPoint;

/**
 * Test module used by {@see ProducerIntegrationTest}.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@EntryPoint public class ProducerTestModule {
  private static ProducerTestModule instance;

  //@Inject @A
  private Integer injectedNumberA;

  @Produces @A 
  private Integer numberA = new Random().nextInt();

  //@Inject @B
  private Integer injectedNumberB;

  private Integer numberB;

  @Produces @B 
  public Integer produceNumberB() {
    numberB = new Random().nextInt();
    return numberB;
  }

  //@Inject
  private String injectedString;

  @Produces 
  public String produceString(@C Integer number) {
    return new Integer(number).toString();
  }

  private Integer numberC;

  @Produces @C 
  public Integer produceNumberC() {
    numberC = new Random().nextInt();
    return numberC;
  }

  public Integer getInjectedNumberA() {
    return injectedNumberA;
  }

  public Integer getNumberA() {
    return numberA;
  }

  public Integer getNumberB() {
    return numberB;
  }

  public Integer getInjectedNumberB() {
    return injectedNumberB;
  }

  public String getInjectedString() {
    return injectedString;
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
}