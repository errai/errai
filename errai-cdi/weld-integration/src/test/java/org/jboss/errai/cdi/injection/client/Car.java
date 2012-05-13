package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.Dependent;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@Dependent
public class Car {
  public static boolean success;

  @Inject
  Petrol petrol;

  public Car() {
      success = false;
  }

  public String getName() {
      return "herbie";
  }

  public String getNameOfPetrol() {
      return petrol.getName();
  }
}
