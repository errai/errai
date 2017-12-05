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
import org.jboss.errai.common.apt.localapps.localapp2.TestLocalErraiApp2;
import org.jboss.errai.common.apt.localapps.localapp2.module2.TestExportedType21;
import org.jboss.errai.common.apt.localapps.localapp2.module2.TestExportedType22;
import org.jboss.errai.common.configuration.ErraiApp;
import org.jboss.errai.common.configuration.ErraiModule;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Optional;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ErraiAptExportedTypesTest extends ErraiAptTest {

  @ErraiApp(gwtModuleName = "")
  private static final class TestApp {
  }

  @ErraiModule
  private static final class TestModule {

  }

  private MetaClass erraiAppAnnotatedMetaClass;

  @Before
  public void before() {
    this.erraiAppAnnotatedMetaClass = aptClass(TestApp.class);
  }

  @Test
  public void testFindAnnotatedMetaClassesWithNoExportedOrLocallyExportableTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass,
            resourceFilesFinder(), elements);

    Assert.assertTrue(
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesUnusedTestAnnotation.class).isEmpty());
  }

  @Test
  public void testFindAnnotatedMetaClassesWithNoLocallyExportableTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass,
            resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(ErraiAptExportedTypesTestExportedType.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesWithSameTypeAsExportedAndLocallyExportable() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass,
            resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(ErraiAptExportedTypesTestExportedType.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesWithOneTypeAsExportedAndOneAsLocallyExportable() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(erraiAppAnnotatedMetaClass,
            resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(ErraiAptExportedTypesTestExportedType.class),
            aptClass(ErraiAptExportedTypesLocallyExportableType2.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithThreeExportedTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(
            aptClass(TestLocalAppWithTwoSubErraiApps.class), resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType1.class), aptClass(TestExportedType21.class),
            aptClass(TestExportedType22.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithOneExportedType() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(aptClass(TestLocalErraiApp1.class),
            resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType1.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithTwoExportedTypes() {
    final ErraiAptExportedTypes erraiAptExportedTypes = new ErraiAptExportedTypes(aptClass(TestLocalErraiApp2.class),
            resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType21.class), aptClass(TestExportedType22.class)),
            erraiAptExportedTypes.findAnnotatedMetaClasses(ErraiAptExportedTypesTestAnnotation.class));
  }

  private ResourceFilesFinder resourceFilesFinder() {
    return a -> Optional.empty();
  }
}