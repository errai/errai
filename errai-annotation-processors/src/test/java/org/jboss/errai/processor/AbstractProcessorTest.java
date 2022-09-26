/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.tools.Diagnostic;
import javax.tools.Diagnostic.Kind;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

/**
 * Base class for Errai annotation checker tests.
 * <p>
 * This code is based on a corresponding class from the UberFire test suite.
 */
public abstract class AbstractProcessorTest {

  /**
   * Warning messages that don't count against {@link #assertSuccessfulCompilation(List)}.
   */
  private final Set<String> ignorableWarnings = new HashSet<>(
          Arrays.asList(
                  "bootstrap class path not set in conjunction with -source", 
                  "Implicitly compiled files were not subject to annotation processing.",
                  "Recompile with",
                  "Some input files use or override a deprecated API",
                  "uses or overrides a deprecated API",
                  "Some input files use unchecked or unsafe operations"));

  /**
   * Compile a unit of source code with the specified annotation processor
   *
   * @param annotationProcessor
   *          the annotation processor that should participate in the
   *          compilation
   * @param compilationUnit
   *          path to a classpath resource, eg
   *          "org/jboss/errai/processor/testcase/TemplatedNotExtendingComposite.java"
   */
  public List<Diagnostic<? extends JavaFileObject>> compile(final String compilationUnit) {

    final DiagnosticCollector<JavaFileObject> diagnosticListener = new DiagnosticCollector<>();

    try {

      final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
      final StandardJavaFileManager fileManager = compiler.getStandardFileManager(diagnosticListener, null, null);

      // Convert compilation unit to file path and add to items to compile
      final String path = this.getClass().getResource("/" + compilationUnit).getPath();
      final Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjects(path);

      // Compile with provided annotation processor
      final CompilationTask task;
      if (compiler.isSupportedOption("--release") == -1 ) {
        task = compiler.getTask(null, fileManager, diagnosticListener, Arrays.asList("-source", "1.8", "-target", "1.8"), null, compilationUnits);
      } else {
        task = compiler.getTask(null, fileManager, diagnosticListener, Arrays.asList("--release", "8"), null, compilationUnits);
      }
      
      task.setProcessors(Arrays.asList(getProcessorUnderTest()));
      task.call();

      fileManager.close();

    } catch (final IOException ioe) {
      fail(ioe.getMessage());
    }

    return diagnosticListener.getDiagnostics();
  }

  /**
   * Checks that there are no unignorable errors or warnings in the given list of diagnostics.
   */
  public void assertSuccessfulCompilation(final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    final StringBuilder sb = new StringBuilder(100);

    nextMessage:
    for (final Diagnostic<? extends JavaFileObject> msg : diagnostics) {
      for (final String ignorableWarning : ignorableWarnings) {
        if (msg.getMessage(null).contains(ignorableWarning)) {
          continue nextMessage;
        }
      }
      sb.append(msg.getKind())
        .append(" ")
        .append(msg.getLineNumber())
        .append(":")
        .append(msg.getColumnNumber())
        .append(": ")
            .append(msg.getMessage(null))
        .append("\n");
    }
    if (sb.length() > 0) {
      fail("Expected no warnings or errors; got:\n" + sb);
    }
  }

  /**
   * Assert that compilation failed
   *
   * @param diagnostics
   */
  public void assertFailedCompilation(final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    assertTrue(hasErrors(diagnostics));
  }

  private boolean hasErrors(final List<Diagnostic<? extends JavaFileObject>> diagnostics) {
    for (final Diagnostic<? extends JavaFileObject> diagnostic : diagnostics) {
      if (diagnostic.getKind().equals(Kind.ERROR)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Assert that the given error message is contained in the compilation
   * diagnostics.
   *
   * @param diagnostics
   *          the list of diagnostic messages from the compiler. Must not be null.
   * @param kind
   *          the kind of message to search for, or null to search messages of
   *          any kind.
   * @param line
   *          the line number that must be attached to the message, or
   *          {@link Diagnostic#NOPOS} if line number is not important.
   * @param col
   *          the column number that must be attached to the message, or
   *          {@link Diagnostic#NOPOS} if column number is not important.
   * @param message
   *          the message to search for. If any otherwise matching message in
   *          the given list contains this string, the assertion passes. Must not be null.
   */
  public void assertCompilationMessage(final List<Diagnostic<? extends JavaFileObject>> diagnostics, final Kind kind, final long line, final long col, final String message) {
    final StringBuilder sb = new StringBuilder(100);
    for (final Diagnostic<? extends JavaFileObject> msg : diagnostics) {
      sb.append(msg.getKind())
        .append(" ")
        .append(msg.getLineNumber())
        .append(":")
        .append(msg.getColumnNumber())
        .append(": ")
        .append(msg.getMessage(null))
        .append("\n");
      if ( (kind == null || msg.getKind().equals(kind))
              && (line == Diagnostic.NOPOS || msg.getLineNumber() == line)
              && (col == Diagnostic.NOPOS) || msg.getColumnNumber() == col
              && msg.getMessage(null).contains(message)) {
        return;
      }
    }

    fail("Compiler diagnostics did not contain " + kind + " message " + line + ":" + col + ": " + message + "\n" +
            "Dump of all " + diagnostics.size() + " actual messages:\n" +
            sb);
  }

  /**
   * Returns the annotation processor being tested by the current test. This
   * processor should be created with a GenerationCompleteCallback that will
   * capture the output of the processor so it can be examined by test
   * assertions.
   */
  protected abstract AbstractProcessor getProcessorUnderTest();
}
