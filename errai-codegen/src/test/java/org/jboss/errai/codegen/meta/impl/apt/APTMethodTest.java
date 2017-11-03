/*
 * Copyright (C) 2017 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.codegen.meta.impl.apt;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaClassFactory;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.type.TypeMirror;
import java.util.List;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class APTMethodTest extends ErraiAptTest {

  @Test
  public void testGetReturnTypeConcreteClass() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("foo", withNoParameters());
    Assert.assertEquals("java.lang.String", method.getReturnType().toString());
    Assert.assertEquals(metaClass(String.class), method.getReturnType());
  }

  @Test
  public void testGetReturnTypeConcreteInterface() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteInterface.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getMethod("foo", withNoParameters());
    Assert.assertEquals("java.lang.String", method.getReturnType().toString());
    Assert.assertEquals(metaClass(String.class), method.getReturnType());
  }

  @Test
  public void testGetReturnTypeGenericInterface() {
    final TypeMirror typeMirror = getTypeElement(TestGenericInterface.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("foo", withNoParameters());
    Assert.assertEquals("T", method.getReturnType().toString());
  }

  @Test
  public void testGetReturnTypeGenericReturnType() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("bar", withNoParameters());
    Assert.assertEquals("java.lang.Object", method.getReturnType().toString());
    Assert.assertEquals(metaClass(Object.class), method.getReturnType());
  }

  @Test
  public void testGetReturnTypeBoundedGenericReturnType() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("boundedBar", withNoParameters());
    Assert.assertEquals("java.lang.Long", method.getReturnType().toString());
    Assert.assertEquals(metaClass(Long.class), method.getReturnType());
  }

  @Test
  public void testGetReturnTypeWildcard() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("wildcardBar", withNoParameters());
    Assert.assertEquals("java.util.List<?>", method.getReturnType().toString());
  }

  @Test
  public void testGetParameters() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("par", String.class);
    Assert.assertEquals(1, method.getParameters().length);
    Assert.assertEquals(metaClass(String.class), method.getParameters()[0].getType());
  }

  @Test
  public void testGetParametersGeneric() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("unboundedPar", String.class, Object.class);
    Assert.assertEquals(2, method.getParameters().length);
    Assert.assertEquals(metaClass(String.class), method.getParameters()[0].getType());
    Assert.assertEquals(metaClass(Object.class), method.getParameters()[1].getType());
  }

  @Test
  public void testGetParametersBoundedGeneric() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteClass.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("boundedPar", String.class, Long.class);
    Assert.assertEquals(2, method.getParameters().length);
    Assert.assertEquals(metaClass(String.class), method.getParameters()[0].getType());
    Assert.assertEquals(metaClass(Long.class), method.getParameters()[1].getType());
  }

  @Test
  public void testGetParametersConcreteInterface() {
    final TypeMirror typeMirror = getTypeElement(TestConcreteInterface.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getMethod("par", String.class);
    Assert.assertEquals(1, method.getParameters().length);
    Assert.assertEquals(metaClass(String.class), method.getParameters()[0].getType());
  }

  @Test
  public void testGetParametersGenericInterface() {
    final TypeMirror typeMirror = getTypeElement(TestGenericInterface.class).asType();
    final MetaMethod method = new APTClass(typeMirror).getDeclaredMethod("par", Object.class);
    Assert.assertEquals(1, method.getParameters().length);
    Assert.assertEquals(metaClass(Object.class), method.getParameters()[0].getType());
  }

  private static MetaClass metaClass(final Class<?> clazz) {
    return MetaClassFactory.get(clazz);
  }

  private static MetaClass[] withNoParameters() {
    return new MetaClass[0];
  }
}