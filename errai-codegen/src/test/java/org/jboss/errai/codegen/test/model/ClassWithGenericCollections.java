package org.jboss.errai.codegen.test.model;

import java.io.Serializable;
import java.util.Collection;

/**
 * A class with fields of various generic types, for testing the MetaClass implementations.
 *
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class ClassWithGenericCollections<T> {
  Collection<String> hasStringParam;
  Collection<? extends String> hasWildcardExtendsStringParam;
  Collection<T> hasUnboundedTypeVarFromClass;

  <B extends Serializable> B hasSingleBoundedTypeVarFromSelf() {
    return null;
  }

  <B extends Collection & Serializable> B hasDoubleBoundedTypeVarFromSelf() {
    return null;
  }
}
