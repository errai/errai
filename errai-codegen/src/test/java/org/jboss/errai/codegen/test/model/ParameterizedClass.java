package org.jboss.errai.codegen.test.model;

public class ParameterizedClass<T> {

  public T methodReturningTypeParameter() {
    return null;
  }

  public void methodAcceptingTypeParam(T param) {
    // no op
  }
}
