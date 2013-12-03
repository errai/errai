package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

@Dependent
public class DepBeanProducer {
  
  @Produces
  public Produceable produce() {
    return new Produceable();
  }

}
