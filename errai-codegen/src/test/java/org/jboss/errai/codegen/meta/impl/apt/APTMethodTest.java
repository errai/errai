package org.jboss.errai.codegen.meta.impl.apt;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.type.TypeMirror;

public class APTMethodTest extends ErraiAptTest {


  @Test
  public void testGetReturnTypeConcreteClass() {
    TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    MetaMethod[] methods = new APTClass(typeMirror).getMethods();

    Assert.assertEquals("java.lang.String", methods[11].getReturnType().toString());
  }

  @Test
  public void testGetReturnTypeConcreteInterface() {
    TypeMirror typeMirror = getTypeElement(TestConcreteInterface.class).asType();
    MetaMethod[] methods = new APTClass(typeMirror).getMethods();

    Assert.assertEquals("java.lang.String", methods[9].getReturnType().toString());
  }

  @Test
  public void testGetReturnTypeGenericInterface() {
    TypeMirror typeMirror = getTypeElement(TestGenericInterface.class).asType();
    MetaMethod[] methods = new APTClass(typeMirror).getDeclaredMethods();

    Assert.assertEquals(1, methods.length);
    Assert.assertEquals("T", methods[0].getReturnType().toString());
  }

}