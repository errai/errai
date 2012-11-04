package org.jboss.errai.ioc.async.test.client.res;

import javax.inject.Inject;

/**
 * @author Mike Brock
 */
public class Bar {
  @Inject BazTheSingleton bazTheSingleton;

  public BazTheSingleton getBazTheSingleton() {
    return bazTheSingleton;
  }
}
