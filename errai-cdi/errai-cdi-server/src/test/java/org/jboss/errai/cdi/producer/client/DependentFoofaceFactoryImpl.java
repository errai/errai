package org.jboss.errai.cdi.producer.client;

import javax.enterprise.context.ApplicationScoped;


/**
 * @author Mike Brock
 */
@ApplicationScoped
public class DependentFoofaceFactoryImpl implements DependentFoofaceFactory {

  @Override
  public Fooface getFooface() {
    return new Fooface() {
      @Override
      public String getMessage() {
        return "HiThere";
      }
    };
  }
}
