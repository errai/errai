package org.jboss.errai.codegen.test.model;

import java.util.Collection;

/**
 * A class with methods returning and accepting parameters of various generic
 * types, for testing the MetaClass implementations.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class ClassWithGenericMethods {

  public Object methodReturningObject() {
    return null;
  }

  public Collection<?> methodReturningUnboundedWildcardCollection() {
    return null;
  }

  public Collection<? extends String> methodReturningUpperBoundedWildcardCollection() {
    return null;
  }

  public Collection<? super String> methodReturningLowerBoundedWildcardCollection() {
    return null;
  }
}
