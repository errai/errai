package org.jboss.errai.cdi.async.test.producers.client.res;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class DepBeanProducer {
  
  @Produces
  public Produceable produce() {
    return new Produceable();
  }

}
