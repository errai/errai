package org.jboss.errai.cdi.injection.client;

import javax.enterprise.inject.Specializes;

@Specializes
@Lazy
public class LazyFarmer extends Farmer {

  @Override
  public String getClassName() {
    return LazyFarmer.class.getName();
  }

}