package org.jboss.errai.ioc.async.test.beanmanager.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;

import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@LoadAsync
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
