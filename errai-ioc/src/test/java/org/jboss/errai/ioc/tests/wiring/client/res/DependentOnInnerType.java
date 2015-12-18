package org.jboss.errai.ioc.tests.wiring.client.res;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;

@ApplicationScoped
public class DependentOnInnerType {

  @Inject
  private InnerType inner;

  public InnerType getInner() {
    return inner;
  }

  @Dependent
  public static class InnerType {

  }

}
