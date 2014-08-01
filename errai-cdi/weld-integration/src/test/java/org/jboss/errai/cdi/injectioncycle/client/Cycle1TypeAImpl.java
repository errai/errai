package org.jboss.errai.cdi.injectioncycle.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class Cycle1TypeAImpl implements Cycle1TypeA {

  @Inject
  private Cycle1TypeB bInstance;

  @Override
  public Cycle1TypeB getBInstance() {
    return bInstance;
  }
}
