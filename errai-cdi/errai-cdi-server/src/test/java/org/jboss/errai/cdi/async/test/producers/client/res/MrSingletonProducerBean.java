package org.jboss.errai.cdi.async.test.producers.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author Mike Brock
 */
@ApplicationScoped @LoadAsync
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
