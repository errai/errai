package org.jboss.errai.cdi.async.test.producers.client.res;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

/**
 * @author Mike Brock
 */
@Dependent
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
