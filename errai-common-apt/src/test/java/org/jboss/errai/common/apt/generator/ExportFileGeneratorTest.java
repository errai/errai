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

package org.jboss.errai.common.apt.generator;

import org.jboss.errai.codegen.apt.test.ErraiAptTest;
import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.TestAnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.common.configuration.ErraiModule;
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ExportFileGeneratorTest extends ErraiAptTest {

  @ErraiModule
  private static final class TestModule {

  }

  @Test
  public void testGenerateExportFilesForUsedAnnotation() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableType.class);
    final TypeElement testModule = getTypeElement(TestModule.class);

    final TestGenerator testGenerator = getTestGenerator(singleton(testAnnotation),
            annotatedElementsFinder(testExportedType, testModule));
    final Set<ExportFile> exportFiles = testGenerator.createExportFiles();

    Assert.assertEquals(1, exportFiles.size());
    final ExportFile exportFile = exportFiles.stream().findFirst().get();
    Assert.assertEquals(testAnnotation, exportFile.annotation());
    Assert.assertEquals(singleton(testExportedType.asType()), exportFile.exportedTypes());
  }

  @Test
  public void testBuildExportFilesForUnusedAnnotation() {
    final Set<TypeElement> annotations = singleton(getTypeElement(TestUnusedAnnotation.class));
    final Set<ExportFile> exportFiles = getTestGenerator(annotations, annotatedElementsFinder()).createExportFiles();

    Assert.assertEquals(0, exportFiles.size());
  }

  @Test
  public void testBuildExportFilesForEmptySetOfAnnotations() {
    final TestGenerator testGenerator = getTestGenerator(emptySet(), annotatedElementsFinder());
    final Set<ExportFile> exportFiles = testGenerator.createExportFiles();
    Assert.assertEquals(0, exportFiles.size());
  }

  private TestAnnotatedSourceElementsFinder annotatedElementsFinder(final Element... typeElements) {
    return new TestAnnotatedSourceElementsFinder(typeElements);
  }

  private TestGenerator getTestGenerator(final Set<? extends TypeElement> annotations,
          final AnnotatedSourceElementsFinder annotatedElementsFinder) {
    return new TestGenerator(annotations, annotatedElementsFinder, elements);
  }
}