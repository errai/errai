package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


/**
 * @author Mike Brock
 */
@ApplicationScoped
public class SingletonProducedBeanDependentBean {
  @Inject Kayak kayakA;
  @Inject Kayak kayakB;

  public Kayak getKayakA() {
    return kayakA;
  }

  public Kayak getKayakB() {
    return kayakB;
  }
}
