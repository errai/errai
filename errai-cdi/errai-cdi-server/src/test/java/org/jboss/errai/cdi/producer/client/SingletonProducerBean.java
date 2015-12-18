package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;


/**
 * @author Mike Brock
 */
@ApplicationScoped
public class SingletonProducerBean {
  private static int count = 0;

  @ApplicationScoped @Produces
  private Kayak produceKayek() {
    return new Kayak(++count);
  }
}
