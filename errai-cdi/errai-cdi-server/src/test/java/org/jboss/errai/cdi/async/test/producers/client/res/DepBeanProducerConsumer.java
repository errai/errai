package org.jboss.errai.cdi.async.test.producers.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@Dependent
public class DepBeanProducerConsumer {

  @Inject private Produceable producable;
  
  public Produceable getProducable() {
    return producable;
  }
  
}
