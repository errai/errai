package org.jboss.errai.cdi.injectioncycle.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Cycle1TypeBImpl implements Cycle1TypeB {

  @Inject
  Cycle1TypeA aInstance;

  @Override
  public Cycle1TypeA getAInstance() {
    return aInstance;
  }
}
