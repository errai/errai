package org.jboss.errai.codegen.test.meta.java;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.jboss.errai.codegen.test.meta.AbstractMetaClassTest;

public class JavaReflectionMetaClassTest extends AbstractMetaClassTest {

  @Override
  protected MetaClass getMetaClassImpl(Class<?> javaClass) {
    return JavaReflectionClass.newInstance(javaClass);
  }

  @Override
  protected Class<? extends MetaClass> getTypeOfMetaClassBeingTested() {
    return JavaReflectionClass.class;
  }

}
