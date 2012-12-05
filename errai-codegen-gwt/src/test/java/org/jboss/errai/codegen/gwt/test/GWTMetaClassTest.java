package org.jboss.errai.codegen.gwt.test;

import java.io.File;

import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.gwt.GWTClass;
import org.jboss.errai.codegen.test.meta.AbstractMetaClassTest;
import org.jboss.errai.codegen.test.model.PrimitiveFieldContainer;

import com.google.gwt.core.ext.typeinfo.TypeOracle;

/**
 * The GWT implementation of the overall MetaClass test.
 *
 * @author Mike Brock
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class GWTMetaClassTest extends AbstractMetaClassTest {

  private static final TypeOracle mockacle;
  static {
    MockacleFactory f = new MockacleFactory(new File("../errai-codegen/src/test/java"));
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.Child");
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.Grandparent");
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.GrandparentInterface");
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.GrandparentSuperInterface");
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.IsolatedInterface");
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.Parent");
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.ParentInterface");
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.ParentSuperInterface1");
    f.addTestClass("org.jboss.errai.codegen.test.model.tree.ParentSuperInterface2");
    f.addTestClass("org.jboss.errai.codegen.test.model.TestInterface");
    f.addTestClass("org.jboss.errai.codegen.test.model.ObjectWithNested");
    f.addTestClass(PrimitiveFieldContainer.class.getName());

    mockacle = f.generateMockacle();
  }

  @Override
  protected MetaClass getMetaClassImpl(Class<?> javaClass) {

    int dims = 0;
    while (javaClass.isArray()) {
      javaClass = javaClass.getComponentType();
      dims++;
    }

    MetaClass metaClass;
    if (javaClass.isPrimitive()) {
      // This is a hack for getting a JType for a primitive
      // (I couldn't find any Source implementation that does it directly)
      MetaClass container = GWTClass.newInstance(mockacle, PrimitiveFieldContainer.class.getName());
      metaClass = container.getDeclaredField(javaClass.getName() + "Field").getType();
    }
    else {
      metaClass = GWTClass.newInstance(mockacle, javaClass.getName());
    }

    if (metaClass == null) {
      throw new RuntimeException("Oops, the mock TypeOracle doesn't know about " + javaClass);
    }

    if (dims > 0) {
      metaClass = metaClass.asArrayOf(dims);
    }

    return metaClass;
  }

  @Override
  protected Class<? extends MetaClass> getTypeOfMetaClassBeingTested() {
    return GWTClass.class;
  }

  // NOTE: all of the test methods are inherited from AbstractMetaClassTest

}
//  @Test
//  public void confirmContractConsistency() throws Exception {
//    final String classToTest = "foo.MyTestClass";
//
//    MetaClassFactory.emptyCache();
//
//    final MetaClass gwtMC = GWTClass.newInstance(mockacle, mockacle.getType(classToTest));
//    final MetaClass javaMC = JavaReflectionClass.newUncachedInstance(loadTestClass(classToTest));
//
//    assertEquals(gwtMC.getSuperClass(), javaMC.getSuperClass());
//
//    assertEquals(gwtMC.getName(), javaMC.getName());
//    assertEquals(gwtMC.getFullyQualifiedName(), javaMC.getFullyQualifiedName());
//    assertEquals(gwtMC.getCanonicalName(), javaMC.getCanonicalName());
//    assertEquals(gwtMC.getInternalName(), javaMC.getInternalName());
//
//    assertEquals(gwtMC.isPublic(), javaMC.isPublic());
//    assertEquals(gwtMC.isProtected(), javaMC.isProtected());
//    assertEquals(gwtMC.isPrivate(), javaMC.isPrivate());
//
//    assertEquals(gwtMC.isDefaultInstantiable(), javaMC.isDefaultInstantiable());
//
//    System.out.println("--gwt methods--");
//    for (MetaMethod method : gwtMC.getDeclaredMethods()) {
//      System.out.println(method.toString());
//    }
//
//    System.out.println("--java methods--");
//    for (MetaMethod method : javaMC.getDeclaredMethods()) {
//      System.out.println(method.toString());
//    }
//
//    assertEquals(new HashSet<MetaMethod>(Arrays.asList(gwtMC.getDeclaredMethods())),
//                 new HashSet<MetaMethod>(Arrays.asList(javaMC.getDeclaredMethods())));
//
//    final MetaClass gwtSuperMC = gwtMC.getSuperClass();
//    final MetaClass javaSuperMC = javaMC.getSuperClass();
//
//    assertEquals(gwtSuperMC.getFullyQualifiedName(), javaSuperMC.getFullyQualifiedName());
//    assertEquals(1, gwtSuperMC.getInterfaces().length);
//    assertEquals(1, javaSuperMC.getInterfaces().length);
//
//    assertEquals(new HashSet<MetaClass>(Arrays.asList(gwtSuperMC.getInterfaces())),
//                 new HashSet<MetaClass>(Arrays.asList(javaSuperMC.getInterfaces())));
//  }
//
//  @Test
//  public void confirmContractConsistencyForStringTypeParam() throws Exception {
//    final String classToTest = "foo.ClassWithGenericCollections";
//
//    MetaClassFactory.emptyCache();
//
//    final MetaClass gwtMC = GWTClass.newInstance(mockacle, mockacle.getType(classToTest));
//    final MetaClass javaMC = JavaReflectionClass.newUncachedInstance(loadTestClass(classToTest));
//
//    MetaField gwtField = gwtMC.getDeclaredField("hasStringParam");
//    MetaField javaField = javaMC.getDeclaredField("hasStringParam");
//
//    assertEquals(gwtField, javaField);
//
//    // The type (should be foo.TypeWithTypeParam<String>)
//    assertEquals("ClassWithTypeParam", javaField.getType().getName());
//    assertEquals("ClassWithTypeParam", gwtField.getType().getName());
//
//    assertEquals("foo.ClassWithTypeParam", javaField.getType().getFullyQualifiedName());
//    assertEquals("foo.ClassWithTypeParam", gwtField.getType().getFullyQualifiedName());
//
//    assertEquals("<java.lang.String>", javaField.getType().getParameterizedType().toString());
//    assertEquals("<java.lang.String>", gwtField.getType().getParameterizedType().toString());
//
//    assertEquals("foo.ClassWithTypeParam<java.lang.String>", javaField.getType().getFullyQualifiedNameWithTypeParms());
//    assertEquals("foo.ClassWithTypeParam<java.lang.String>", gwtField.getType().getFullyQualifiedNameWithTypeParms());
//
//    assertEquals("foo.ClassWithTypeParam", javaField.getType().getErased().getFullyQualifiedNameWithTypeParms());
//    assertEquals("foo.ClassWithTypeParam", gwtField.getType().getErased().getFullyQualifiedNameWithTypeParms());
//
//    gwtField.getType().equals(javaField.getType());
//    assertEquals(gwtField.getType(), javaField.getType());
//
//    assertEquals(
//            Arrays.asList(gwtField.getType().getParameterizedType().getTypeParameters()),
//            Arrays.asList(javaField.getType().getParameterizedType().getTypeParameters()));
//  }
//
//  @Test
//  public void confirmContractConsistencyForStringWildcardTypeParam() throws Exception {
//    final String classToTest = "foo.ClassWithGenericCollections";
//
//    MetaClassFactory.emptyCache();
//
//    final MetaClass gwtMC = GWTClass.newInstance(mockacle, mockacle.getType(classToTest));
//    final MetaClass javaMC = JavaReflectionClass.newUncachedInstance(loadTestClass(classToTest));
//
//    MetaField gwtField = gwtMC.getDeclaredField("hasWildcardExtendsStringParam");
//    MetaField javaField = javaMC.getDeclaredField("hasWildcardExtendsStringParam");
//
//    assertEquals(gwtField, javaField);
//
//    // The type (should be foo.TypeWithTypeParam<String>)
//    assertEquals("ClassWithTypeParam", javaField.getType().getName());
//    assertEquals("ClassWithTypeParam", gwtField.getType().getName());
//
//    assertEquals("foo.ClassWithTypeParam", javaField.getType().getFullyQualifiedName());
//    assertEquals("foo.ClassWithTypeParam", gwtField.getType().getFullyQualifiedName());
//
//    assertEquals("<? extends java.lang.String>", javaField.getType().getParameterizedType().toString());
//    assertEquals("<? extends java.lang.String>", gwtField.getType().getParameterizedType().toString());
//
//    assertEquals("foo.ClassWithTypeParam<? extends java.lang.String>", javaField.getType().getFullyQualifiedNameWithTypeParms());
//    assertEquals("foo.ClassWithTypeParam<? extends java.lang.String>", gwtField.getType().getFullyQualifiedNameWithTypeParms());
//
//    assertEquals("foo.ClassWithTypeParam", javaField.getType().getErased().getFullyQualifiedNameWithTypeParms());
//    assertEquals("foo.ClassWithTypeParam", gwtField.getType().getErased().getFullyQualifiedNameWithTypeParms());
//
//    gwtField.getType().equals(javaField.getType());
//    assertEquals(gwtField.getType(), javaField.getType());
//
//    assertEquals(
//            Arrays.asList(gwtField.getType().getParameterizedType().getTypeParameters()),
//            Arrays.asList(javaField.getType().getParameterizedType().getTypeParameters()));
//  }
//
//  @Test
//  public void testNoDuplicateMethodsInClassHierarchy() throws NotFoundException {
//    final MetaClass gwtMC = GWTClass.newInstance(mockacle, mockacle.getType("foo.TestModel"));
//
//    List<MetaMethod> foundCompareMethods = new ArrayList<MetaMethod>();
//    for (MetaMethod m : gwtMC.getMethods()) {
//      if (m.getName().equals("compare")) {
//        foundCompareMethods.add(m);
//      }
//    }
//
//    assertEquals("Only one compare method should have been found", 1, foundCompareMethods.size());
//  }
