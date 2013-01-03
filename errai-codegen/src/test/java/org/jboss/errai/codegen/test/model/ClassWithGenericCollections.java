package org.jboss.errai.codegen.test.model;

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
}
