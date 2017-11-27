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
import org.jboss.errai.common.apt.localapps.TestLocalAppWithTwoSubErraiApps;
import org.jboss.errai.common.apt.localapps.localapp1.TestLocalErraiApp1;
import org.jboss.errai.common.apt.localapps.localapp1.module1.TestExportedType1;
import org.jboss.errai.common.apt.localapps.localapp1.module1.TestModule1;
import org.jboss.errai.common.apt.localapps.localapp2.TestLocalErraiApp2;
import org.jboss.errai.common.apt.localapps.localapp2.module2.TestExportedType21;
import org.jboss.errai.common.apt.localapps.localapp2.module2.TestExportedType22;
import org.jboss.errai.common.apt.localapps.localapp2.module2.TestModule2;
import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.common.configuration.ErraiModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAptExportedTypesTest extends ErraiAptTest {

  @ErraiApp(gwtModuleName = "")
  private static final class TestApp {
  }

  private MetaClass erraiAppAnnotatedMetaClass;

  @Before
  public void before() {
    this.erraiAppAnnotatedMetaClass = aptClass(TestApp.class);
  }

  @Test
  public void testFindAnnotatedMetaClassesWithNoExportedOrLocallyExportableTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass, elements,
            new TestAnnotatedSourceElementsFinder(), resourceFilesFinder());

    Assert.assertTrue(
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesUnusedTestAnnotation.class).isEmpty());
  }

  @Test
  public void testFindAnnotatedMetaClassesWithNoLocallyExportableTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass, elements,
            new TestAnnotatedSourceElementsFinder(), resourceFilesFinder());

    Assert.assertEquals(ImmutableSet.of(aptClass(ErraiAptExportedTypesTestExportedType.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesWithSameTypeAsExportedAndLocallyExportable() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass, elements,
            new TestAnnotatedSourceElementsFinder(getTypeElement(ErraiAptExportedTypesTestExportedType.class)),
            resourceFilesFinder());

    Assert.assertEquals(ImmutableSet.of(aptClass(ErraiAptExportedTypesTestExportedType.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesWithOneTypeAsExportedAndOneAsLocallyExportable() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass, elements,
            new TestAnnotatedSourceElementsFinder(getTypeElement(ErraiAptExportedTypesLocallyExportableType2.class)),
            resourceFilesFinder());

    Assert.assertEquals(ImmutableSet.of(aptClass(ErraiAptExportedTypesTestExportedType.class),
            aptClass(ErraiAptExportedTypesLocallyExportableType2.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithThreeExportedTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(
            aptClass(TestLocalAppWithTwoSubErraiApps.class), elements, localErraiAppAnnotatedSourceElementsFinder(),
            resourceFilesFinder());

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType1.class), aptClass(TestExportedType21.class),
            aptClass(TestExportedType22.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithOneExportedType() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(aptClass(TestLocalErraiApp1.class),
            elements, localErraiAppAnnotatedSourceElementsFinder(), resourceFilesFinder());

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType1.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithTwoExportedTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(aptClass(TestLocalErraiApp2.class),
            elements, localErraiAppAnnotatedSourceElementsFinder(), resourceFilesFinder());

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType21.class), aptClass(TestExportedType22.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  private AnnotatedSourceElementsFinder localErraiAppAnnotatedSourceElementsFinder() {
    return new TestAnnotatedSourceElementsFinder() {

      @Override
          public Set<? extends Element> findSourceElementsAnnotatedWith(TypeElement typeElement) {
            if (typeElement.equals(getTypeElement(ErraiAptExportedTypesTestAnnotation.class))) {
          return ImmutableSet.of(getTypeElement(TestExportedType1.class), getTypeElement(TestExportedType21.class),
                  getTypeElement(TestExportedType22.class));
        } else {
          return Collections.emptySet();
        }
      }

      @Override
      public Set<? extends Element> findSourceElementsAnnotatedWith(final Class<? extends Annotation> annotationClass) {
        if (annotationClass.equals(ErraiModule.class)) {
          return ImmutableSet.of(getTypeElement(TestModule1.class), getTypeElement(TestModule2.class));
        } else {
          return Collections.emptySet();
        }
      }
    };
  }

  private ResourceFilesFinder resourceFilesFinder() {
    return a -> Optional.empty();
  }
}