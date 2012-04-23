package org.jboss.errai.codegen.test.model;

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
