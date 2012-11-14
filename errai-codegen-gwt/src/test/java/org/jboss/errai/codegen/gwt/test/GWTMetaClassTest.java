package org.jboss.errai.codegen.gwt.test;

import java.util.Arrays;
import java.util.HashSet;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.meta.impl.java.JavaReflectionClass;
import org.junit.Test;

import com.google.gwt.core.ext.typeinfo.NotFoundException;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

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
    addTestClass("foo.TestModelInterface");
    addTestClass("foo.AbstractSuperTestModel");
    addTestClass("foo.SuperTestModel");
    addTestClass("foo.TestModel");

    mockacle = generateMockacle();
  }

  @Test
  public void confirmContractConsistency() throws Exception {
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

    assertEquals(new HashSet<MetaMethod>(Arrays.asList(gwtMC.getDeclaredMethods())),
                 new HashSet<MetaMethod>(Arrays.asList(javaMC.getDeclaredMethods())));

    final MetaClass gwtSuperMC = gwtMC.getSuperClass();
    final MetaClass javaSuperMC = javaMC.getSuperClass();

    assertEquals(gwtSuperMC.getFullyQualifiedName(), javaSuperMC.getFullyQualifiedName());
    assertEquals(1, gwtSuperMC.getInterfaces().length);
    assertEquals(1, javaSuperMC.getInterfaces().length);

    assertEquals(new HashSet<MetaClass>(Arrays.asList(gwtSuperMC.getInterfaces())),
                 new HashSet<MetaClass>(Arrays.asList(javaSuperMC.getInterfaces())));
  }
  
  @Test
  public void testNoDuplicateMethodsInClassHierarchy() throws NotFoundException {
    final MetaClass gwtMC = GWTClass.newInstance(mockacle, mockacle.getType("foo.TestModel"));
    
    List<MetaMethod> foundCompareMethods = new ArrayList<MetaMethod>();
    for (MetaMethod m : gwtMC.getMethods()) {
      if (m.getName().equals("compare")) {
        foundCompareMethods.add(m);
      }
    }
    
    assertEquals("Only one compare method should have been found", 1, foundCompareMethods.size());
  }
}
