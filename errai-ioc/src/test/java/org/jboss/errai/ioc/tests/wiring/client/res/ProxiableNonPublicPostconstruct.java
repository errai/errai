package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ProxiableNonPublicPostconstruct {

  private boolean value;

  @PostConstruct
  void postConstruct() {
    value = true;
  }

  public boolean getValue() {
    return value;
  }

}
