package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent
public class DepBeanConstrConsumesOwnProducer {
  WrappedThang thang;
  ProducerFactory factory;

  // needed because of cycle in main constructor
  public DepBeanConstrConsumesOwnProducer() {
  }

  @Inject
  public DepBeanConstrConsumesOwnProducer(@Produced WrappedThang thing, ProducerFactory factory) {
    this.thang = thing;
    this.factory = factory;
  }

  @Produces @Produced
  private WrappedThang produceWrappedThing(Thang thing) {
    return new WrappedThang(thing);
  }

  public WrappedThang getThang() {
    return thang;
  }

  public ProducerFactory getFactory() {
    return factory;
  }
}
