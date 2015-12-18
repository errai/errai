package org.jboss.errai.codegen.gwt.test;

import static org.junit.Assert.*;

import java.io.File;

import org.jboss.errai.codegen.meta.MetaType;
import org.jboss.errai.codegen.meta.impl.gwt.GWTParameterizedType;
import org.junit.Test;

import com.google.gwt.core.ext.typeinfo.JClassType;
import com.google.gwt.core.ext.typeinfo.JType;
import com.google.gwt.core.ext.typeinfo.TypeOracle;

public class GWTParameterizedTypeTest {

  private static final TypeOracle mockacle;
  static {
    MockacleFactory f = new MockacleFactory(new File(
            "../errai-codegen/src/test/java"));
    f.addTestClass("org.jboss.errai.codegen.test.model.GenericArrayCollectionTestModel");

    mockacle = f.generateMockacle();
  }

  @Test
  public void getTypeParameters_GenericArray() {
    JClassType type = mockacle
            .findType("org.jboss.errai.codegen.test.model.GenericArrayCollectionTestModel");
    JType returnType = type.getMethods()[0].getReturnType();
    GWTParameterizedType parameterizedType = new GWTParameterizedType(mockacle,
            returnType.isParameterized());
    MetaType[] typeParameters = parameterizedType.getTypeParameters();
    assertEquals(typeParameters.length, 1);
    assertEquals(typeParameters[0].getName(), "Object[]");
  }

}
