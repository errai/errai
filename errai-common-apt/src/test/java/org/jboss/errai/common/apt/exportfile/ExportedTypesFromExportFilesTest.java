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

package org.jboss.errai.common.apt.exportfile;

import com.google.common.collect.ImmutableSet;
import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.codegen.meta.MetaClass;
import org.jboss.errai.common.apt.generator.app.ResourceFilesFinder;
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
public class ExportedTypesFromExportFilesTest extends ErraiAptTest {

  @ErraiApp(gwtModuleName = "")
  private static final class TestApp {
  }

  private MetaClass erraiAppAnnotatedMetaClass;

  @Before
  public void before() {
    this.erraiAppAnnotatedMetaClass = aptClass(TestApp.class);
  }

  @Test
  public void testFindAnnotatedMetaClassesWithNoExportedTypes() {
    final ExportedTypesFromExportFiles exportedTypesFromExportFiles = new ExportedTypesFromExportFiles(
            erraiAppAnnotatedMetaClass, resourceFilesFinder(), elements);

    Assert.assertTrue(
            exportedTypesFromExportFiles.findAnnotatedMetaClasses(ExportedTypesFromExportFilesUnusedTestAnnotation.class)
                    .isEmpty());
  }

  @Test
  public void testFindAnnotatedMetaClassesOfNonLocalErraiApp() {
    final ExportedTypesFromExportFiles exportedTypesFromExportFiles = new ExportedTypesFromExportFiles(
            erraiAppAnnotatedMetaClass, resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(ExportedTypesFromExportFilesTestExportedType.class),
            aptClass(TestExportedType1.class), aptClass(TestExportedType21.class), aptClass(TestExportedType22.class)),
            exportedTypesFromExportFiles.findAnnotatedMetaClasses(ExportedTypesFromExportFilesTestAnnotation.class));

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType.class)),
            exportedTypesFromExportFiles.findAnnotatedMetaClasses(TestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithThreeExportedTypes() {
    final ExportedTypesFromExportFiles exportedTypesFromExportFiles = new ExportedTypesFromExportFiles(
            aptClass(TestLocalAppWithTwoSubErraiApps.class), resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType1.class), aptClass(TestExportedType21.class),
            aptClass(TestExportedType22.class)),
            exportedTypesFromExportFiles.findAnnotatedMetaClasses(ExportedTypesFromExportFilesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithOneExportedType() {
    final ExportedTypesFromExportFiles exportedTypesFromExportFiles = new ExportedTypesFromExportFiles(
            aptClass(TestLocalErraiApp1.class), resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType1.class)),
            exportedTypesFromExportFiles.findAnnotatedMetaClasses(ExportedTypesFromExportFilesTestAnnotation.class));
  }

  @Test
  public void testFindAnnotatedMetaClassesForLocalErraiAppWithTwoExportedTypes() {
    final ExportedTypesFromExportFiles exportedTypesFromExportFiles = new ExportedTypesFromExportFiles(
            aptClass(TestLocalErraiApp2.class), resourceFilesFinder(), elements);

    Assert.assertEquals(ImmutableSet.of(aptClass(TestExportedType21.class), aptClass(TestExportedType22.class)),
            exportedTypesFromExportFiles.findAnnotatedMetaClasses(ExportedTypesFromExportFilesTestAnnotation.class));
  }

  private ResourceFilesFinder resourceFilesFinder() {
    return a -> Optional.empty();
  }
}