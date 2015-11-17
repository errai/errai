package org.jboss.errai.cdi.async.test.producers.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped @LoadAsync
public class AsyncSingletonProducerDependentBean {
  @Inject LaBean laBean;
  @Inject LaBean laBean2;

  public LaBean getLaBean() {
    return laBean;
  }

  public LaBean getLaBean2() {
    return laBean2;
  }
}
