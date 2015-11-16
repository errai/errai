package org.jboss.errai.cdi.specialization.client;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Produces;

/**
 * @author Mike Brock
 */
@Dependent
public class Building {
  protected String getClassName() {
    return Building.class.getName();
  }

  @Produces
  public Waste getWaste() {
    return new Waste(getClassName());
  }
}
