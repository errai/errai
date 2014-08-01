/*
 * Copyright 2012 JBoss Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
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
 * Tests for the {@code @DataField} annotation checker.
 */
public class DataFieldAnnotationCheckerTest extends AbstractProcessorTest {

  @Override
  protected AbstractProcessor getProcessorUnderTest() {
    return new DataFieldAnnotationChecker();
  }

  @Test
  public void shouldPrintErrorOnFieldNotExtendingWidget() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/DataFieldNotWidget.java");

    assertCompilationMessage(diagnostics, Kind.ERROR, 10, 21, "must be assignable to Widget");
  }

  @Test
  public void shouldCompileCleanlyWhenAllRulesAreFollowed() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/DataFieldNoWarnings.java");

    assertSuccessfulCompilation(diagnostics);
  }
  
  @Test
  public void shouldCompileCleanlyWhenAllRulesAreFollowedInSubTemplate() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/DataFieldNoWarningsSubtemplate.java");

    assertSuccessfulCompilation(diagnostics);
  }

  @Test
  public void shouldPrintWarningOnDataFieldInNonTemplatedClass() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/DataFieldOutsideTemplatedClass.java");

    assertCompilationMessage(diagnostics, Kind.WARNING, 9, 3, "no effect");
  }
  

}
