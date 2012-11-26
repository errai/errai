package org.jboss.errai.cdi.async.test.producers.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

/**
 * @author Mike Brock
 */
@Dependent @LoadAsync
public class StringProducerBean {
  @Produces
  @Autumn
  @Dependent
  private String produceStringAutumn() {
    return "Autumn";
  }

  @Produces
  @Petunia
  @Dependent
  private String produceStringPetunia() {
    return "Petunia";
  }
}
