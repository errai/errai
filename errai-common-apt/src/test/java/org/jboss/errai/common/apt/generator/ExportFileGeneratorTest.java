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
import org.junit.Assert;
import org.junit.Test;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Optional;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class ExportFileGeneratorTest extends ErraiAptTest {

  @Test
  public void testGenerateExportFilesForUsedAnnotation() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableType.class);

    final TestGenerator testGenerator = getTestGenerator(singleton(testAnnotation),
            annotatedElementsFinder(testExportedType));
    final Set<ExportFile> exportFiles = testGenerator.buildExportFiles();

    Assert.assertEquals(1, exportFiles.size());
    final ExportFile exportFile = exportFiles.stream().findFirst().get();
    Assert.assertEquals(testAnnotation, exportFile.annotation);
    Assert.assertEquals(singleton(testExportedType), exportFile.exportedTypes);
  }

  @Test
  public void testBuildExportFilesForUnusedAnnotation() {
    final Set<TypeElement> annotations = singleton(getTypeElement(TestUnusedAnnotation.class));
    final Set<ExportFile> exportFiles = getTestGenerator(annotations, annotatedElementsFinder()).buildExportFiles();

    Assert.assertEquals(0, exportFiles.size());
  }

  @Test
  public void testBuildExportFilesForEmptySetOfAnnotations() {
    final TestGenerator testGenerator = getTestGenerator(emptySet(), annotatedElementsFinder());
    final Set<ExportFile> exportFiles = testGenerator.buildExportFiles();
    Assert.assertEquals(0, exportFiles.size());
  }

  @Test
  public void testAnnotatedClassesAndInterfacesForAnnotatedField() {
    final Element[] testExportedType = getTypeElement(
            TestExportableTypeWithFieldAnnotations.class).getEnclosedElements().toArray(new Element[0]);

    final TypeElement testEnclosedElementAnnotation = getTypeElement(TestEnclosedElementAnnotation.class);

    final TestGenerator testGenerator = getTestGenerator(emptySet(), annotatedElementsFinder(testExportedType));

    final Set<? extends Element> elements = testGenerator.annotatedClassesAndInterfaces(testEnclosedElementAnnotation);
    Assert.assertTrue(elements.isEmpty());
  }

  @Test
  public void testAnnotatedClassesAndInterfacesForAnnotatedClass() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);

    final TestGenerator testGenerator = getTestGenerator(emptySet(), annotatedElementsFinder(testExportedType));

    final Set<? extends Element> elements = testGenerator.annotatedClassesAndInterfaces(testAnnotation);
    Assert.assertEquals(singleton(testExportedType), elements);
  }

  @Test
  public void testNewExportFileWithOneExportedType() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);
    final TestAnnotatedSourceElementsFinder annotatedElementsFinder = annotatedElementsFinder(testExportedType);

    final TestGenerator testGenerator = getTestGenerator(emptySet(), annotatedElementsFinder);
    final Optional<ExportFile> exportFile = testGenerator.newExportFile(testAnnotation);

    Assert.assertTrue(exportFile.isPresent());
    Assert.assertEquals(1, exportFile.get().exportedTypes.size());
    Assert.assertTrue(exportFile.get().exportedTypes.contains(testExportedType));
  }

  @Test
  public void testNewExportFileWithNoExportedTypes() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);

    final TestGenerator testGenerator = getTestGenerator(emptySet(), annotatedElementsFinder());
    final Optional<ExportFile> exportFile = testGenerator.newExportFile(testAnnotation);

    Assert.assertFalse(exportFile.isPresent());
  }

  private TestAnnotatedSourceElementsFinder annotatedElementsFinder(final Element... typeElements) {
    return new TestAnnotatedSourceElementsFinder(typeElements);
  }

  private TestGenerator getTestGenerator(final Set<? extends TypeElement> annotations,
          final AnnotatedSourceElementsFinder annotatedElementsFinder) {
    return new TestGenerator(null, annotations, annotatedElementsFinder);
  }
}