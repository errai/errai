package org.jboss.errai.ioc.async.test.client.res;

import org.jboss.errai.ioc.client.container.AsyncBeanManager;

import javax.inject.Inject;

/**
 * @author Mike Brock
 */
public class Bar2 {
  @Inject BazTheSingleton bazTheSingleton;
  @Inject AsyncBeanManager manager;

  public AsyncBeanManager getManager() {
    return manager;
  }

  public BazTheSingleton getBazTheSingleton() {
    return bazTheSingleton;
  }
}
