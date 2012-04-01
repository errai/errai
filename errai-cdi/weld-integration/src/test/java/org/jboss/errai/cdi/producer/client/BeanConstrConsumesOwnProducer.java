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

  public BeanConstrConsumesOwnProducer() {
  }

  @Inject
  public BeanConstrConsumesOwnProducer(WrappedThing thing, ProducerFactory factory) {
    this.thing = thing;
    this.factory = factory;
  }

  @Produces
  private WrappedThing produceWrappedThing(Thing thing) {
    return new WrappedThing(thing);
  }

  public WrappedThing getThing() {
    return thing;
  }

  public ProducerFactory getFactory() {
    return factory;
  }
}
