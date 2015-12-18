package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class BeanConsumesOwnProducerB {
  WrappedThing thing;
  ProducerFactory factory;

  @Inject Thung thung;

  // needed because of cycle in main constructor
  public BeanConsumesOwnProducerB() {
  }

  @Inject
  public BeanConsumesOwnProducerB(@Produced WrappedThing thing, ProducerFactory factory) {
    this.thing = thing;
    this.factory = factory;
  }

  @Produces @ApplicationScoped
  private Thung produceThung() {
    return new Thung();
  }

  public WrappedThing getThing() {
    return thing;
  }

  public ProducerFactory getFactory() {
    return factory;
  }

  public Thung getThung() {
    return thung;
  }
}
