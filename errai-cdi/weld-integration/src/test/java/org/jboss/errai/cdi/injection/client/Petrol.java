package org.jboss.errai.cdi.injection.client;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

/**
 * @author Mike Brock
 */
@ApplicationScoped
public class Petrol {
  @Inject
  Car car;

  public String getName() {
    return "petrol";
  }

  public String getNameOfCar() {
    return car.getName();
  }
}
