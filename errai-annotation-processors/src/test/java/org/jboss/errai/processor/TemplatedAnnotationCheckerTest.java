/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.processor;

import java.io.FileNotFoundException;
import java.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.junit.Test;

/**
 * Tests for the {@code @Templated} annotation checker.
 */
public class TemplatedAnnotationCheckerTest extends AbstractProcessorTest {

  @Override
  protected AbstractProcessor getProcessorUnderTest() {
    return new TemplatedAnnotationChecker();
  }

  @Test
  public void shouldPrintErrorOnClassWhenNotExtendingComposite() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/TemplatedNotExtendingComposite.java");

    assertSuccessfulCompilation(diagnostics);
  }

  @Test
  public void shouldPrintErrorOnClassWhenTemplateMissing() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/TemplatedMissingTemplate.java");

    assertCompilationMessage(diagnostics, Kind.ERROR, Diagnostic.NOPOS, Diagnostic.NOPOS,
            "Could not access associated template TemplatedMissingTemplate.html");
  }


  @Test
  public void shouldCompileCleanlyWhenAllRulesAreFollowedInSubTemplate() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/DataFieldNoWarningsSubtemplate.java");

    assertSuccessfulCompilation(diagnostics);
  }

}
