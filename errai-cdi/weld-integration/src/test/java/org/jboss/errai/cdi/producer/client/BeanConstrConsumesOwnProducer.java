package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class BeanConstrConsumesOwnProducer {
  WrappedThing thing;
  ProducerFactory factory;

  // needed because of cycle in main constructor
  public BeanConstrConsumesOwnProducer() {
  }

  @Inject
  public BeanConstrConsumesOwnProducer(@Produced WrappedThing thing, ProducerFactory factory) {
    this.thing = thing;
    this.factory = factory;
  }

  @Produces @Produced
  private WrappedThing produceWrappedThing(@Produced Thing thing) {
    return new WrappedThing(thing);
  }

  public WrappedThing getThing() {
    return thing;
  }

  public ProducerFactory getFactory() {
    return factory;
  }
}
