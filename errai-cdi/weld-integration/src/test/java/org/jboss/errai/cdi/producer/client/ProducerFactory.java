package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class ProducerFactory {
  @Produces
  public Thing produceThing() {
    return new Thing();
  }
}
