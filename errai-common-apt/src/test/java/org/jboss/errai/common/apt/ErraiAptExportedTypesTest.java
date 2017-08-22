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

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.codegen.meta.MetaClass;
import org.junit.Assert;
import org.junit.Test;

import java.util.Collection;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAptExportedTypesTest extends ErraiAptTest {

  @Test
  public void testFindAnnotatedMetaClassesWithNoExportedOrLocallyExportableTypes() {
    ErraiAptExportedTypes.init(types, elements, new TestAnnotatedElementsFinder());

    Collection<MetaClass> annotatedMetaClasses = ErraiAptExportedTypes.findAnnotatedMetaClasses(
            ErraiAptExportedTypesUnusedTestAnnotation.class);

    Assert.assertEquals(0, annotatedMetaClasses.size());
  }

  @Test
  public void testFindAnnotatedMetaClassesWithNoLocallyExportableTypes() {
    ErraiAptExportedTypes.init(types, elements, new TestAnnotatedElementsFinder());

    Collection<MetaClass> annotatedMetaClasses = ErraiAptExportedTypes.findAnnotatedMetaClasses(
            ErraiAptExportedTypesTestAnnotation.class);

    Assert.assertEquals(1, annotatedMetaClasses.size());
  }

  @Test
  public void testFindAnnotatedMetaClassesWithSameTypeAsExportedAndLocallyExportable() {
    ErraiAptExportedTypes.init(types, elements,
            new TestAnnotatedElementsFinder(getTypeElement(ErraiAptExportedTypesTestExportedType.class)));

    Collection<MetaClass> annotatedMetaClasses = ErraiAptExportedTypes.findAnnotatedMetaClasses(
            ErraiAptExportedTypesTestAnnotation.class);

    Assert.assertEquals(1, annotatedMetaClasses.size());
  }

  @Test
  public void testFindAnnotatedMetaClassesWithOneTypeAsExportedAndOneAsLocallyExportable() {
    ErraiAptExportedTypes.init(types, elements,
            new TestAnnotatedElementsFinder(getTypeElement(ErraiAptExportedTypesLocallyExportableType2.class)));

    Collection<MetaClass> annotatedMetaClasses = ErraiAptExportedTypes.findAnnotatedMetaClasses(
            ErraiAptExportedTypesTestAnnotation.class);

    Assert.assertEquals(2, annotatedMetaClasses.size());
  }

}