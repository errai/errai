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

import org.jboss.errai.common.apt.AnnotatedElementsFinder;
import org.jboss.errai.common.apt.TestAnnotatedElementsFinder;
import org.jboss.errai.common.apt.exportfile.ExportFile;
import org.jboss.errai.codegen.apt.test.ErraiAptTest;
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
public class AbstractErraiModuleExportFileGeneratorTest extends ErraiAptTest {

  @Test
  public void testThrownExceptionDoesNotBreakIt() {
    Assert.assertFalse(new TestGenerator() {
      @Override
      void generateAndSaveExportFiles(final Set<? extends TypeElement> annotations,
              final AnnotatedElementsFinder annotatedElementsFinder) {
        throw new TestException();
      }
    }.process(null, null));
  }

  @Test
  public void testProcessForEmptyAnnotationsSet() {
    Assert.assertFalse(new TestGenerator().process(emptySet(), null));
  }

  @Test
  public void testGenerateExportFilesForUsedAnnotation() {
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);
    final TypeElement testExportedType = getTypeElement(TestExportableType.class);

    final Set<ExportFile> exportFiles = new TestGenerator().buildExportFiles(singleton(testAnnotation),
            new TestAnnotatedElementsFinder(testExportedType));

    Assert.assertEquals(1, exportFiles.size());
    final ExportFile exportFile = exportFiles.stream().findFirst().get();
    Assert.assertEquals(testAnnotation, exportFile.annotation);
    Assert.assertEquals(singleton(testExportedType), exportFile.exportedTypes);
  }

  @Test
  public void testBuildExportFilesForUnusedAnnotation() {
    final Set<TypeElement> annotations = singleton(getTypeElement(TestUnusedAnnotation.class));
    final Set<ExportFile> exportFiles = new TestGenerator().buildExportFiles(annotations,
            new TestAnnotatedElementsFinder());

    Assert.assertEquals(0, exportFiles.size());
  }

  @Test
  public void testBuildExportFilesForEmptySetOfAnnotations() {
    final Set<ExportFile> exportFiles = new TestGenerator().buildExportFiles(emptySet(), null);
    Assert.assertEquals(0, exportFiles.size());
  }

  @Test
  public void testAnnotatedClassesAndInterfacesForAnnotatedField() {
    final Element[] testExportedType = getTypeElement(
            TestExportableTypeWithFieldAnnotations.class).getEnclosedElements().toArray(new Element[0]);
    final TypeElement TestEnclosedElementAnnotation = getTypeElement(TestEnclosedElementAnnotation.class);

    final Set<? extends Element> elements = new TestGenerator().annotatedClassesAndInterfaces(
            new TestAnnotatedElementsFinder(testExportedType), TestEnclosedElementAnnotation);

    Assert.assertTrue(elements.isEmpty());
  }

  @Test
  public void testAnnotatedClassesAndInterfacesForAnnotatedClass() {
    final TypeElement testExportedType = getTypeElement(TestExportableTypeWithFieldAnnotations.class);
    final TypeElement testAnnotation = getTypeElement(TestAnnotation.class);

    final Set<? extends Element> elements = new TestGenerator().annotatedClassesAndInterfaces(
            new TestAnnotatedElementsFinder(testExportedType), testAnnotation);

    Assert.assertEquals(singleton(testExportedType), elements);
  }
}