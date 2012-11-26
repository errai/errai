package org.jboss.errai.cdi.async.test.producers.client.res;

import org.jboss.errai.ioc.client.api.LoadAsync;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped @LoadAsync
public class AsyncProducerDependentBean {
  private final MaBean maBean;
  private final MaBean maBean2;

  @Inject
  public AsyncProducerDependentBean(final MaBean maBean, final MaBean maBean2) {
    this.maBean = maBean;
    this.maBean2 = maBean2;
  }

  public MaBean getMaBean() {
    return maBean;
  }

  public MaBean getMaBean2() {
    return maBean2;
  }
}
