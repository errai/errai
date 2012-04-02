package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class ProducerTestModuleWithCycle {
  private final ProducerTestModule testModule;
  private final DependentFoofaceFactory foofaceFactory;

  @Inject
  public ProducerTestModuleWithCycle(ProducerTestModule testModule, DependentFoofaceFactory foofaceFactory) {
    this.testModule = testModule;
    this.foofaceFactory = foofaceFactory;
  }

  @Produces
  public Fooface getFooface() {
    return foofaceFactory.getFooface();
  }
}
