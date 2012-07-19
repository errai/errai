package org.jboss.errai.codegen.gwt.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.junit.Test;

/**
 * @author Mike Brock
 */
public class GWTMetaClassTests extends AbstractGWTMetaClassTest {
  private final TypeOracle mockacle;

  public GWTMetaClassTests() {
    addTestClass("foo.MyTestClass");
    mockacle = generateMockacle();
  }

  @Test
  public void confirmContractConsistency1() throws Exception {
    final String classToTest = "foo.MyTestClass";

    final MetaClass gwtMC = GWTClass.newInstance(mockacle, mockacle.getType(classToTest));
    final MetaClass javaMC = JavaReflectionClass.newUncachedInstance(loadTestClass(classToTest));

    assertEquals(gwtMC.getSuperClass(), javaMC.getSuperClass());

    assertEquals(gwtMC.getName(), javaMC.getName());
    assertEquals(gwtMC.getFullyQualifiedName(), javaMC.getFullyQualifiedName());
    assertEquals(gwtMC.getCanonicalName(), javaMC.getCanonicalName());
    assertEquals(gwtMC.getInternalName(), javaMC.getInternalName());

    assertEquals(gwtMC.isPublic(), javaMC.isPublic());
    assertEquals(gwtMC.isProtected(), javaMC.isProtected());
    assertEquals(gwtMC.isPrivate(), javaMC.isPrivate());

    assertEquals(gwtMC.isDefaultInstantiable(), javaMC.isDefaultInstantiable());

    assertArrayEquals(gwtMC.getDeclaredMethods(), javaMC.getDeclaredMethods());
  }
}
