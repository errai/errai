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
import org.jboss.errai.codegen.meta.MetaConstructor;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public abstract class MetaConstructorTest extends ErraiAptTest {

  @Test
  public void unboundedParameter() {
    final MetaConstructor constructor = getMetaClass(TestAbstractClass.class).getDeclaredConstructor(Object.class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void unboundedArrayParameter() {
    final MetaConstructor constructor = getMetaClass(TestAbstractClass.class).getDeclaredConstructor(Object[].class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void boundedParameter() {
    final MetaConstructor constructor = getMetaClass(TestAbstractClass.class).getDeclaredConstructor(Number.class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void twoGenerics() {
    final MetaConstructor constructor = getMetaClass(TestAbstractClass.class).getDeclaredConstructor(Object.class, Object.class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void threeGenerics() {
    final MetaConstructor constructor = getMetaClass(TestAbstractClass.class).getDeclaredConstructor(Object.class, Number.class, Object.class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void concreteUnboundedParameter() {
    final MetaConstructor constructor = getMetaClass(TestConcreteClass.class).getDeclaredConstructor(String.class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void concreteBoundedArray() {
    final MetaConstructor constructor = getMetaClass(TestConcreteClass.class).getDeclaredConstructor(String[].class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void concreteBoundedParameter() {
    final MetaConstructor constructor = getMetaClass(TestConcreteClass.class).getDeclaredConstructor(Long.class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void concreteTwoGenerics() {
    final MetaConstructor constructor = getMetaClass(TestConcreteClass.class).getDeclaredConstructor(String.class, Long.class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void concreteThreeGenerics() {
    final MetaConstructor constructor = getMetaClass(TestConcreteClass.class).getDeclaredConstructor(String.class, Long.class, Object.class);
    Assert.assertNotNull(constructor);
  }

  @Test
  public void concreteGenericArray() {
    final MetaConstructor constructor = getMetaClass(TestConcreteClass.class).getDeclaredConstructor(Object[].class);
    Assert.assertNotNull(constructor);
  }

  protected abstract MetaClass getMetaClass(final Class<?> clazz);
}
