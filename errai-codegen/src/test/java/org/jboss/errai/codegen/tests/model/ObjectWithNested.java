package org.jboss.errai.codegen.tests.model;

/**
 * @author Mike Brock
 */
public class ObjectWithNested {
  public class MyNestedInterface implements TestInterface {

  }

  public MyNestedInterface getMyNestedInterface() {
    return new MyNestedInterface();
  }
}
