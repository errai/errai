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
import org.jboss.errai.codegen.meta.MetaEnum;
import org.jboss.errai.codegen.meta.RuntimeEnum;
import org.jboss.errai.codegen.meta.RuntimeAnnotation;
import org.junit.Assert;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.jboss.errai.codegen.meta.impl.apt.TestEnum.Bar;
import static org.jboss.errai.codegen.meta.impl.apt.TestEnum.Foo;

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
  public void testStringArrayValueAsObject() {
    final Optional<MetaAnnotation> annotation = new APTClass(
            getTypeElement(TestAnnotatedClass3.class).asType()).getAnnotation(TestAnnotationWithArrayProperties.class);

    Assert.assertTrue(annotation.isPresent());
    Assert.assertEquals(singletonList("foo"), asList(annotation.get().value()));
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

  @Test
  public void testEnumArrayValue() {
    final Optional<MetaAnnotation> annotation = new APTClass(
            getTypeElement(TestAnnotatedClass4.class).asType()).getAnnotation(TestAnnotationWithArrayProperties.class);

    Assert.assertTrue(annotation.isPresent());

    final List<MetaEnum> expectedValue = asList(new RuntimeEnum(Foo), new RuntimeEnum(Bar));
    final List<MetaEnum> actualValue = asList(annotation.get().valueAsArray("enums", MetaEnum[].class));

    Assert.assertEquals(expectedValue, actualValue);
  }

  @Test
  public void testAnnotationArrayValue() {
    final Optional<MetaAnnotation> annotation = new APTClass(
            getTypeElement(TestAnnotatedClass4.class).asType()).getAnnotation(TestAnnotationWithArrayProperties.class);

    Assert.assertTrue(annotation.isPresent());

    final List<MetaAnnotation> expectedValue = asList(newRuntimeTestAnnotation("foo"), newRuntimeTestAnnotation("bar"));
    final List<MetaAnnotation> actualValue = asList(annotation.get().valueAsArray("annotations", MetaAnnotation[].class));

    Assert.assertEquals(expectedValue, actualValue);
  }

  private MetaAnnotation newRuntimeTestAnnotation(final String string) {
    return new RuntimeAnnotation(new TestInnerAnnotation() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return TestInnerAnnotation.class;
      }

      @Override
      public String value() {
        return string;
      }
    });
  }
}
