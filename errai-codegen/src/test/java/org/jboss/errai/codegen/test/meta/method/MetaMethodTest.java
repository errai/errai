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

package org.jboss.errai.codegen.test.meta.method;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.MetaMethod;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class MetaMethodTest extends ErraiAptTest {

  @Test
  public void testGetReturnTypeConcreteClass() {
    final MetaMethod method = getMetaClass(TestConcreteClass.class).getDeclaredMethod("foo", withNoParameters());
    Assert.assertEquals("java.lang.String", method.getReturnType().toString());
    Assert.assertEquals(getMetaClass(String.class), method.getReturnType());
  }

  @Test
  public void testGetReturnTypeConcreteInterface() {
    final MetaMethod method = getMetaClass(TestConcreteInterface.class).getMethod("foo", withNoParameters());
    Assert.assertEquals("java.lang.String", method.getReturnType().toString());
    Assert.assertEquals(getMetaClass(String.class), method.getReturnType());
  }

  @Test
  public void testGetReturnTypeGenericInterface() {
    final MetaMethod method = getMetaClass(TestGenericInterface.class).getDeclaredMethod("foo", withNoParameters());
    Assert.assertEquals("java.lang.Object", method.getReturnType().toString());
  }

  @Test
  public void testGetReturnTypeGenericReturnType() {
    final MetaMethod method = getMetaClass(TestConcreteClass.class).getDeclaredMethod("bar", withNoParameters());
    Assert.assertEquals("java.lang.Object", method.getReturnType().toString());
    Assert.assertEquals(getMetaClass(Object.class), method.getReturnType());
  }

  @Test
  public void testGetReturnTypeBoundedGenericReturnType() {
    final MetaMethod method = getMetaClass(TestConcreteClass.class).getDeclaredMethod("boundedBar", withNoParameters());
    Assert.assertEquals("java.lang.Long", method.getReturnType().toString());
    Assert.assertEquals(getMetaClass(Long.class), method.getReturnType());
  }

  @Test
  public void testGetReturnTypeWildcard() {
    final MetaMethod method = getMetaClass(TestConcreteClass.class).getDeclaredMethod("wildcardBar",
            withNoParameters());
    Assert.assertEquals("java.util.List<?>", method.getReturnType().toString());
  }

  @Test
  public void testGetReturnTypeConcreteTypeParameter() {
    final MetaMethod method = getMetaClass(TestConcreteClass.class).getDeclaredMethod("concreteBar",
            withNoParameters());
    Assert.assertEquals("java.util.List<java.lang.String>", method.getReturnType().toString());
  }

  @Test
  public void testGetParameters() {
    final MetaMethod method = getMetaClass(TestConcreteClass.class).getDeclaredMethod("par", String.class);
    Assert.assertEquals(1, method.getParameters().length);
    Assert.assertEquals(getMetaClass(String.class), method.getParameters()[0].getType());
  }

  @Test
  public void testGetParametersGeneric() {
    final MetaMethod method = getMetaClass(TestConcreteClass.class).getDeclaredMethod("unboundedPar", String.class,
            Object.class);
    Assert.assertEquals(2, method.getParameters().length);
    Assert.assertEquals(getMetaClass(String.class), method.getParameters()[0].getType());
    Assert.assertEquals(getMetaClass(Object.class), method.getParameters()[1].getType());
  }

  @Test
  public void testGetParametersBoundedGeneric() {
    final MetaMethod method = getMetaClass(TestConcreteClass.class).getDeclaredMethod("boundedPar", String.class,
            Long.class);
    Assert.assertEquals(2, method.getParameters().length);
    Assert.assertEquals(getMetaClass(String.class), method.getParameters()[0].getType());
    Assert.assertEquals(getMetaClass(Long.class), method.getParameters()[1].getType());
  }

  @Test
  public void testGetParametersConcreteInterface() {
    final MetaMethod method = getMetaClass(TestConcreteInterface.class).getMethod("par", String.class);
    Assert.assertEquals(1, method.getParameters().length);
    Assert.assertEquals(getMetaClass(String.class), method.getParameters()[0].getType());
  }

  @Test
  public void testGetParametersGenericInterface() {
    final MetaMethod method = getMetaClass(TestGenericInterface.class).getDeclaredMethod("par", Object.class);
    Assert.assertEquals(1, method.getParameters().length);
    Assert.assertEquals(getMetaClass(Object.class), method.getParameters()[0].getType());
  }

  protected abstract MetaClass getMetaClass(final Class<?> clazz);

  private static MetaClass[] withNoParameters() {
    return new MetaClass[0];
  }
}