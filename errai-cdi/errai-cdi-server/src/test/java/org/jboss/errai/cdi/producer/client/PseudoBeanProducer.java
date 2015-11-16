package org.jboss.errai.cdi.producer.client;

import javax.enterprise.inject.Produces;

public class PseudoBeanProducer {
  
  @Produces
  public PseudoProduceable produce() {
    return new PseudoProduceable();
  }

}
