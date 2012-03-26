package org.jboss.errai.codegen.framework.tests.gwt.client;

/**
 * @author Mike Brock
 */
public class TypeWithNestedClass {
  public class MyNested implements TestInterface {

  }

  public MyNested getMyNested() {
    return new MyNested();
  }
}
