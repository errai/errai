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

import org.jboss.errai.common.apt.AnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.AptAnnotatedSourceElementsFinder;
import org.jboss.errai.common.apt.TestAnnotatedSourceElementsFinder;
import org.junit.Assert;
import org.junit.Test;

import javax.annotation.processing.Filer;
import javax.lang.model.element.TypeElement;
import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * @author Tiago Bento <tfernand@redhat.com>
 */
public class AbstractErraiModuleExportFileGeneratorTest {

  @Test
  public void testThrownExceptionDoesNotBreakIt() {
    try {
      new AbstractErraiModuleExportFileGenerator() {
        @Override
        protected String getCamelCaseErraiModuleName() {
          return "test";
        }

        @Override
        void generateAndSaveExportFiles(Set<? extends TypeElement> exportableAnnotations,
                AnnotatedSourceElementsFinder annotatedSourceElementsFinder,
                Filer filer) {
          throw new TestException();
        }
      }.process(null, null, null, null, new TestAnnotatedSourceElementsFinder(null));
    } catch (final Exception e) {
      Assert.fail("No exception should've been thrown");
    }
  }

  @Test
  public void testProcessForEmptyAnnotationsSet() {
    try {
      new AbstractErraiModuleExportFileGenerator() {
        @Override
        protected String getCamelCaseErraiModuleName() {
          return "test";
        }
      }.process(emptySet(), null, null, null, new TestAnnotatedSourceElementsFinder());
    } catch (final Exception e) {
      Assert.fail("No exception should've been thrown");
    }
  }

}