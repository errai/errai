package org.jboss.errai.cdi.async.test.producers.client.res;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class MrProducerBean {
  @Produces
  public MaBean produceMaBean(final Foogu foogu) {
    return new MaBean() {
      @Override
      public Foogu getFoogu() {
        return foogu;
      }
    };
  }
 }


