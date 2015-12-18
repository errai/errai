package org.jboss.errai.bus.client.tests.support;

public interface Lifeform {

  // this method "shadows" the public field Person.publicSuperField.
  // This situation is a regression test for ERRAI-439.
  public String getPublicSuperField();

}
