package org.jboss.errai.cdi.async.test.producers.client.res;

import javax.enterprise.inject.Produces;

public class PseudoBeanProducer {
  
  @Produces
  public PseudoProduceable produce() {
    return new PseudoProduceable();
  }

}
