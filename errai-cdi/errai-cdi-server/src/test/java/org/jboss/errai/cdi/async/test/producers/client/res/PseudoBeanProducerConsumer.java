package org.jboss.errai.cdi.async.test.producers.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class PseudoBeanProducerConsumer {

  @Inject private PseudoProduceable producable;
  
  public PseudoProduceable getProducable() {
    return producable;
  }
  
}
