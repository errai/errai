package org.jboss.errai.cdi.producer.client;

import java.util.Random;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Produces;

import org.jboss.errai.cdi.client.qualifier.A;
import org.jboss.errai.cdi.client.qualifier.B;
import org.jboss.errai.cdi.client.qualifier.C;
import org.jboss.errai.cdi.client.qualifier.D;
import org.jboss.errai.cdi.client.qualifier.E;

/**
 * Test module used by {@see ProducerIntegrationTest}.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
@ApplicationScoped
public class ProducerTestModule {
  @Produces
  private static StaticallyProducedBeanB staticallyProducedBeanB = new StaticallyProducedBeanB();

  @Produces @A
  private Integer numberA = new Random().nextInt();

  private Integer numberB;

  @Produces @B
  public Integer produceNumberB() {
    numberB = new Random().nextInt();
    return numberB;
  }

  private Integer numberC;

  @Produces @C
  public Integer produceNumberC() {
    numberC = 1000;
    return numberC;
  }

  @Produces
  private String produceString(@C Integer number) {
    return Integer.toString(number);
  }

  @Produces @D @E @Default
  private Float floatDE = 1.1f;

  @Produces
  private static StaticallyProducedBean produceStaticallyProducedBean() {
    return new StaticallyProducedBean();
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

  public Float getFloatDE() {
    return floatDE;
  }
}