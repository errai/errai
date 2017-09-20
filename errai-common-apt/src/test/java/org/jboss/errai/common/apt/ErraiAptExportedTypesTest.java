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

package org.jboss.errai.common.apt;

import com.google.common.collect.ImmutableSet;
import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.codegen.meta.impl.apt.APTClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;
import java.util.Set;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAptExportedTypesTest extends ErraiAptTest {


  @Test
  public void testFindAnnotatedMetaClassesWithNoExportedOrLocallyExportableTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(types, elements,
            new TestAnnotatedSourceElementsFinder(), resourceFilesFinder());

    final Collection<MetaClass> annotatedMetaClasses = erraiAptExportedTypes.findAnnotatedMetaClasses(
            ErraiAptExportedTypesUnusedTestAnnotation.class);

    Assert.assertEquals(0, annotatedMetaClasses.size());
  }

  @Test
  public void testFindAnnotatedMetaClassesWithNoLocallyExportableTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(types, elements,
            new TestAnnotatedSourceElementsFinder(), resourceFilesFinder());

    final Collection<MetaClass> annotatedMetaClasses = erraiAptExportedTypes.findAnnotatedMetaClasses(
            ErraiAptExportedTypesTestAnnotation.class);

    final Set<APTClass> expected = ImmutableSet.of(
            new APTClass(getTypeElement(ErraiAptExportedTypesTestExportedType.class).asType()));

    Assert.assertEquals(expected, annotatedMetaClasses);
  }

  @Test
  public void testFindAnnotatedMetaClassesWithSameTypeAsExportedAndLocallyExportable() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(types, elements,
            new TestAnnotatedSourceElementsFinder(getTypeElement(ErraiAptExportedTypesTestExportedType.class)),
            resourceFilesFinder());

    final Collection<MetaClass> annotatedMetaClasses = erraiAptExportedTypes.findAnnotatedMetaClasses(
            ErraiAptExportedTypesTestAnnotation.class);

    final Set<APTClass> expected = ImmutableSet.of(
            new APTClass(getTypeElement(ErraiAptExportedTypesTestExportedType.class).asType()));

    Assert.assertEquals(expected, annotatedMetaClasses);
  }

  @Test
  public void testFindAnnotatedMetaClassesWithOneTypeAsExportedAndOneAsLocallyExportable() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(types, elements,
            new TestAnnotatedSourceElementsFinder(getTypeElement(ErraiAptExportedTypesLocallyExportableType2.class)),
            resourceFilesFinder());

    final Collection<MetaClass> annotatedMetaClasses = erraiAptExportedTypes.findAnnotatedMetaClasses(
            ErraiAptExportedTypesTestAnnotation.class);

    final Collection<MetaClass> expected = ImmutableSet.of(
            new APTClass(getTypeElement(ErraiAptExportedTypesTestExportedType.class).asType()),
            new APTClass(getTypeElement(ErraiAptExportedTypesLocallyExportableType2.class).asType()));

    Assert.assertEquals(expected, annotatedMetaClasses);
  }

  private ResourceFilesFinder resourceFilesFinder() {
    return a -> null;
  }
}