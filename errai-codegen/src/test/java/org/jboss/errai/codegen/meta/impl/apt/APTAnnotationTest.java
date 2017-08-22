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
import org.jboss.errai.codegen.meta.MetaAnnotation;
import org.jboss.errai.codegen.meta.MetaClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class APTAnnotationTest extends ErraiAptTest {

  @Test
  public void testUnusedAnnotation() {
    final Optional<MetaAnnotation> annotation = new APTClass(
            getTypeElement(TestAnnotatedClass1.class).asType()).getAnnotation(TestUnusedAnnotation.class);

    Assert.assertFalse(annotation.isPresent());
  }

  @Test
  public void testDefaultValue() {
    final Optional<MetaAnnotation> annotation = new APTClass(
            getTypeElement(TestAnnotatedClass1.class).asType()).getAnnotation(TestAnnotation.class);

    Assert.assertTrue(annotation.isPresent());
    Assert.assertEquals("", annotation.get().value());
  }

  @Test
  public void testValue() {
    final Optional<MetaAnnotation> annotation = new APTClass(
            getTypeElement(TestAnnotatedClass2.class).asType()).getAnnotation(TestAnnotation.class);

    Assert.assertTrue(annotation.isPresent());
    Assert.assertEquals("foo", annotation.get().value());
  }

  @Test
  public void testStringArrayValue() {
    final Optional<MetaAnnotation> annotation = new APTClass(
            getTypeElement(TestAnnotatedClass3.class).asType()).getAnnotation(TestAnnotationWithArrayProperties.class);

    Assert.assertTrue(annotation.isPresent());
    Assert.assertEquals(singletonList("foo"), asList(annotation.get().valueAsArray(String[].class)));
  }

  @Test
  public void testClassArrayValue() {
    final Optional<MetaAnnotation> annotation = new APTClass(
            getTypeElement(TestAnnotatedClass3.class).asType()).getAnnotation(TestAnnotationWithArrayProperties.class);

    Assert.assertTrue(annotation.isPresent());

    final MetaClass stringMetaClass = new APTClass(getTypeElement(String.class).asType());
    final MetaClass longMetaClass = new APTClass(getTypeElement(Long.class).asType());
    final List<MetaClass> expectedValue = asList(stringMetaClass, longMetaClass);

    final List<MetaClass> actualValue = asList(annotation.get().valueAsArray("classes", MetaClass[].class));
    Assert.assertEquals(expectedValue, actualValue);
  }
}
