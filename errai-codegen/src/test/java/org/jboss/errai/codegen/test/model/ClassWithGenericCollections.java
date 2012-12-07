package org.jboss.errai.codegen.test.model;

import java.util.Collection;

public class ClassWithGenericCollections {
  Collection<String> hasStringParam;
  Collection<? extends String> hasWildcardExtendsStringParam;
}
