package org.jboss.errai.cdi.async.test.producers.client.res;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent
public class ProducedStringConsumingBean {
  @Inject @Autumn String autumnString;
  @Inject @Petunia String petuniaString;

  public String getAutumnString() {
    return autumnString;
  }

  public String getPetuniaString() {
    return petuniaString;
  }
}
