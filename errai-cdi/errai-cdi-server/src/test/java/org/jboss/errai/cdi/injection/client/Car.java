package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent
public class Car {
  @Inject
  Petrol petrol;

  public String getName() {
      return "herbie";
  }

  public String getNameOfPetrol() {
      return petrol.getName();
  }
}
