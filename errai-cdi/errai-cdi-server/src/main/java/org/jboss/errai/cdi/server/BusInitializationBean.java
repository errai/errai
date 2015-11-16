package org.jboss.errai.cdi.server;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.BeanManager;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class BusInitializationBean {
  @Inject
  private BeanManager bm;

  @PostConstruct
  private void init() {
  }
}
