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

import static org.apache.commons.lang3.SystemUtils.JAVA_VERSION;
import static org.jboss.errai.processor.TypeNames.GWT_EVENT;
import static org.jboss.errai.processor.TypeNames.GWT_OPAQUE_DOM_EVENT;

import java.io.FileNotFoundException;
import java.util.List;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.JavaFileObject;

import org.junit.Test;

/**
 * Tests for the {@code @EventHandler} annotation checker. The EventHandler
 * annotation has two modes: "regular" and "sink native." Tests in this class
 * generally have two assertions: one to cover each mode.
 */
public class EventHandlerAnnotationCheckerTest extends AbstractProcessorTest {
  
  @Override
  protected AbstractProcessor getProcessorUnderTest() {
    return new EventHandlerAnnotationChecker();
  }

  @Test
  public void shouldCompileCleanlyWhenAllRulesAreFollowed() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/EventHandlerNoWarnings.java");

    assertSuccessfulCompilation(diagnostics);
  }

  @Test
  public void shouldCompileCleanlyWithEventHandlerForSuperTypeDataField() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/EventHandlerSubType.java");

    assertSuccessfulCompilation(diagnostics);
  }

  @Test
  public void shouldPrintErrorWhenEventHandlerMethodReturnsNonVoid() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/EventHandlerNonVoidReturnType.java");

    assertCompilationMessage(diagnostics, Kind.ERROR, 19, 10, "must return void");
    assertCompilationMessage(diagnostics, Kind.ERROR, 25, 11, "must return void");
  }

  @Test
  public void shouldPrintErrorWhenEventHandlerMethodHasNoArgs() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/EventHandlerNoArguments.java");
    assertCompilationMessage(diagnostics, Kind.ERROR, 19, 8, "Event handling methods must take exactly one argument.");
    assertCompilationMessage(diagnostics, Kind.ERROR, 25, 8, "Event handling methods must take exactly one argument.");
  }

  @Test
  public void shouldPrintErrorWhenEventHandlerMethodHasWrongArgTypes() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/EventHandlerWrongArgumentTypes.java");

    assertCompilationMessage(diagnostics, Kind.ERROR, 19, 8,
            String.format(
                    "Event handling methods must take exactly one argument that is a [%s], [%s], or a native @BrowserEvent.",
                    GWT_OPAQUE_DOM_EVENT, GWT_EVENT));
    assertCompilationMessage(diagnostics, Kind.ERROR, 25, 8,
            "@SinkNative event handling methods must take exactly one argument of type com.google.gwt.user.client.Event");
  }

  @Test
  public void shouldPrintErrorWhenReferencedFieldDoesNotExist() throws FileNotFoundException {        
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/EventHandlerBadFieldReference.java");
    
    int col = changeColForJava11Runtime();
    
    // only need one assertion here, because we don't currently parse the template to see if refs to HTML elements are valid
    assertCompilationMessage(diagnostics, Kind.ERROR, 16, col, "must refer to a field");
  }

  @Test
  public void shouldPrintErrorWhenReferencedFieldIsNotAWidget() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/EventHandlerNonWidgetFieldReference.java");
    
    int col = changeColForJava11Runtime();

    // only need one assertion here, because we don't currently parse the template to see if refs to HTML elements are valid
    assertCompilationMessage(diagnostics, Kind.ERROR, 18, col, "must refer to a field of type Widget");
  }

  private int changeColForJava11Runtime() {
    int col = 3;
    //hack to get different columns as correct
    if ( JAVA_VERSION.startsWith("11") ) col = 17;
    return col;
  }

  @Test
  public void shouldPrintWarningOnEventHandlerInNonTemplatedClass() throws FileNotFoundException {
    final List<Diagnostic<? extends JavaFileObject>> diagnostics = compile(
            "org/jboss/errai/processor/testcase/EventHandlerOutsideTemplatedClass.java");

    assertCompilationMessage(diagnostics, Kind.WARNING, 16, 3, "no effect");
    assertCompilationMessage(diagnostics, Kind.WARNING, 22, 3, "no effect");
  }

}
