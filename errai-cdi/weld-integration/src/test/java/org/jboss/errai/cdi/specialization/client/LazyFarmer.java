package org.jboss.errai.cdi.specialization.client;

import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Specializes;

@Dependent
@Specializes
@Lazy
public class LazyFarmer extends Farmer {
  @Override
  public String getClassName() {
    return LazyFarmer.class.getName();
  }
}