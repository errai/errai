package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;


/**
 * @author Mike Brock
 */
@Dependent
public class DependentProducedBeanDependentBean {
  @Inject Kayak kayakA;
  @Inject Kayak kayakB;

  public Kayak getKayakA() {
    return kayakA;
  }

  public Kayak getKayakB() {
    return kayakB;
  }
}
