package org.jboss.errai.cdi.async.test.producers.client.res;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class MrSingletonProducerBean {
  @Produces
  @ApplicationScoped
  public LaBean produceMaBean(final Foogu foogu) {
    return new LaBean() {
      @Override
      public Foogu getFoogu() {
        return foogu;
      }
    };
  }
}
