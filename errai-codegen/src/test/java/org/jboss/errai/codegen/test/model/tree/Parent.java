package org.jboss.errai.codegen.test.model.tree;

public class Parent extends Grandparent implements ParentInterface {

  private int parentPrivate;
  int parentPackage;
  protected int parentProtected;
  public int parentPublic;

  @Override
  public void interfaceMethodOverriddenMultipleTimes() {}
  
  private void privateMethod() {};
  
  protected void protectedMethod() {};

}
