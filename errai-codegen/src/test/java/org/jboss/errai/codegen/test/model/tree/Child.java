package org.jboss.errai.codegen.test.model.tree;

/**
 * Centre of the "Family Tree" class hierarchy for testing purposes.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public class Child extends Parent {

  private int childPrivate;
  int childPackage;
  protected int childProtected;
  public int childPublic;

  @Override
  public void interfaceMethodOverriddenMultipleTimes() {}

}
