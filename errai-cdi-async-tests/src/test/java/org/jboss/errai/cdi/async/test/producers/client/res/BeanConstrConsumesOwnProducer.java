package org.jboss.errai.cdi.async.test.producers.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped @LoadAsync
public class BeanConstrConsumesOwnProducer {
  WrappedKitten wrappedKitten;

  public BeanConstrConsumesOwnProducer() {
  }

  @Inject
  public BeanConstrConsumesOwnProducer(WrappedKitten wrappedKitten) {
    this.wrappedKitten = wrappedKitten;
  }

  @Produces
  private WrappedKitten producerWrappedKitten(Kitten kitten) {
    return new WrappedKitten(kitten);
  }

  public WrappedKitten getWrappedKitten() {
    return wrappedKitten;
  }
}
