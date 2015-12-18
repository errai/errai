package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class PseudoBeanProducerConsumer {

  @Inject private PseudoProduceable producable;
  
  public PseudoProduceable getProducable() {
    return producable;
  }
  
}
