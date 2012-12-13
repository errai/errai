package org.jboss.errai.cdi.async.test.producers.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent @LoadAsync
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
