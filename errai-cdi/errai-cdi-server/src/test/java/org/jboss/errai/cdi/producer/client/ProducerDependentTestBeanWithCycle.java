package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;


/**
 * @author Mike Brock
 */
@ApplicationScoped
public class ProducerDependentTestBeanWithCycle {
  @Inject
  Fooface fooface;

  public Fooface getFooface() {
    return fooface;
  }
}
