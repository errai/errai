package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;


/**
 * @author Mike Brock
 */
@ApplicationScoped
public class BeanConsumesOwnProducer {
  @Inject Magic magic;

  @Produces
  public Magic produceMagic() {
    return new Magic();
  }

  public Magic getMagic() {
    return magic;
  }
}
