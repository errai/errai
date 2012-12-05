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
    f.addTestClass("org.jboss.errai.codegen.test.model.ClassWithGenericCollections");
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
