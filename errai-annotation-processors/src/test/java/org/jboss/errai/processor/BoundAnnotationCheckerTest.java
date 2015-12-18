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
 * Tests for the {@code @Bound} annotation checker.
 */
public class BoundAnnotationCheckerTest extends AbstractProcessorTest {

  @Override
  protected AbstractProcessor getProcessorUnderTest() {
    return new BoundAnnotationChecker();
  }

  //  @Test
  //  public void shouldPrintErrorOnFieldNotExtendingWidget() throws FileNotFoundException {
  //    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
  //            "org/jboss/errai/processor/testcase/DataFieldNotWidget.java");
  //
  //    assertCompilationMessage(diagnostics, Kind.ERROR, 10, 21, "must be assignable to Widget");
  //  }

  @Test
  public void shouldCompileCleanlyWhenAllRulesAreFollowed() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/BoundNoWarnings.java");

    assertSuccessfulCompilation(diagnostics);
  }

  @Test
  public void shouldPrintErrorWhenBoundThingIsNotAWidget() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/BoundNotAWidget.java");

    assertCompilationMessage(diagnostics, Kind.ERROR, 19, 24, "@Bound must target a type assignable to Widget or Element, or be a JsType element wrapper.");
    assertCompilationMessage(diagnostics, Kind.ERROR, 22, 18, "@Bound must target a type assignable to Widget or Element, or be a JsType element wrapper.");
    assertCompilationMessage(diagnostics, Kind.ERROR, 29, 46, "@Bound must target a type assignable to Widget or Element, or be a JsType element wrapper.");
    assertCompilationMessage(diagnostics, Kind.ERROR, 34, 45, "@Bound must target a type assignable to Widget or Element, or be a JsType element wrapper.");
    assertCompilationMessage(diagnostics, Kind.ERROR, 39, 18, "@Bound must target a type assignable to Widget or Element, or be a JsType element wrapper.");
  }

  @Test
  public void shouldPrintErrorWhenModelPropertyDoesNotExist() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/BoundToNonExistingModelProperty.java");

    assertCompilationMessage(diagnostics, Kind.ERROR, 18, 5,  "The model type BoundModelClass does not have property \"nonProperty1\"");
    assertCompilationMessage(diagnostics, Kind.ERROR, 21, 13, "The model type BoundModelClass does not have property \"nonProperty2\"");
    assertCompilationMessage(diagnostics, Kind.ERROR, 24, 13, "The model type BoundModelClass does not have property \"stillNonProperty6\"");
    assertCompilationMessage(diagnostics, Kind.ERROR, 27, 13, "The model type BoundModelClass does not have property \"property1.property2\"");
    assertCompilationMessage(diagnostics, Kind.ERROR, 35, 44, "The model type BoundModelClass does not have property \"nonProperty5\"");
    assertCompilationMessage(diagnostics, Kind.ERROR, 40, 38, "The model type BoundModelClass does not have property \"nonProperty3\"");
    assertCompilationMessage(diagnostics, Kind.ERROR, 44, 5,  "The model type BoundModelClass does not have property \"nonProperty4\"");
  }

  @Test
  public void shouldPrintErrorWhenNoModelClassIsFound() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/BoundButNoModel.java");

    assertCompilationMessage(diagnostics, Kind.ERROR, 16, 3,  "@Bound requires that this class also contains a @Model or @AutoBound DataBinder");
    assertCompilationMessage(diagnostics, Kind.ERROR, 19, 11, "@Bound requires that this class also contains a @Model or @AutoBound DataBinder");
    assertCompilationMessage(diagnostics, Kind.ERROR, 27, 26, "@Bound requires that this class also contains a @Model or @AutoBound DataBinder");
    assertCompilationMessage(diagnostics, Kind.ERROR, 32, 28, "@Bound requires that this class also contains a @Model or @AutoBound DataBinder");
    assertCompilationMessage(diagnostics, Kind.ERROR, 36, 3,  "@Bound requires that this class also contains a @Model or @AutoBound DataBinder");
  }

}
