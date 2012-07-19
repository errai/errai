package org.jboss.errai.codegen.gwt.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import com.google.gwt.core.ext.typeinfo.TypeOracle;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.junit.Test;

/**
 * @author Mike Brock
 */
public class GWTMetaClassTest extends AbstractGWTMetaClassTest {
  private final TypeOracle mockacle;

  public GWTMetaClassTest() {
    addTestClass("foo.SuperInterface");
    addTestClass("foo.TestInterface");
    addTestClass("foo.MyTestSuperClass");
    addTestClass("foo.MyTestClass");

    mockacle = generateMockacle();
  }

  @Test
  public void confirmContractConsistency1() throws Exception {
    final String classToTest = "foo.MyTestClass";

    MetaClassFactory.emptyCache();

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

    System.out.println("--gwt methods--");
    for (MetaMethod method : gwtMC.getDeclaredMethods()) {
      System.out.println(method.toString());
    }

    System.out.println("--java methods--");
    for (MetaMethod method : javaMC.getDeclaredMethods()) {
      System.out.println(method.toString());
    }

    assertArrayEquals(gwtMC.getDeclaredMethods(), javaMC.getDeclaredMethods());

    final MetaClass gwtSuperMC = gwtMC.getSuperClass();
    final MetaClass javaSuperMC = javaMC.getSuperClass();

    assertEquals(gwtSuperMC.getFullyQualifiedName(), javaSuperMC.getFullyQualifiedName());
    assertEquals(1, gwtSuperMC.getInterfaces().length);
    assertEquals(1, javaSuperMC.getInterfaces().length);

    assertArrayEquals(gwtSuperMC.getInterfaces(), javaSuperMC.getInterfaces());
  }
}
