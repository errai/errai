package org.jboss.errai.cdi.specialization.client;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Specializes;
import javax.inject.Named;

/**
 * @author Mike Brock
 */
@Dependent
@Specializes
@Landowner
@Named
public class Farmer extends Human {
  public String getClassName() {
    return Farmer.class.getName();
  }
}
