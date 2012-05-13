package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class Petrol {
  public static boolean success;

  @Inject
  Car car;

  public Petrol() {
    success = false;
  }

  public String getName() {
    return "petrol";
  }

  public String getNameOfCar() {
    return car.getName();
  }
}
