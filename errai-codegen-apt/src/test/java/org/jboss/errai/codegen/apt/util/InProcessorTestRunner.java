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

package org.jboss.errai.codegen.apt.util;

import static com.google.testing.compile.JavaFileObjects.forSourceLines;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;

/**
 * Runs unit tests inside an annotation processor.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class InProcessorTestRunner extends Runner {

  private final Class<?> testClass;

  public InProcessorTestRunner(final Class<?> testClass) {
    this.testClass = testClass;
  }

  @Override
  public Description getDescription() {
    final Description suite = Description.createSuiteDescription(testClass);
    Arrays
      .stream(testClass.getMethods())
      .filter(m -> m.isAnnotationPresent(Test.class))
      .map(m -> Description.createTestDescription(testClass, m.getName()))
      .forEach(suite::addChild);

    return suite;
  }

  @Override
  public void run(final RunNotifier notifier) {
    final Description suite = getDescription();
    final List<Description> children = suite.getChildren();
    final TestControlService service = new TestControlService(notifier, children.iterator(), suite.getClassName());
    final Compilation compilation = Compiler
      .javac()
      .withProcessors(new UnitTestRunningAnntoationProcessor(service))
      .compile(forSourceLines("org.jboss.errai.codegen.Dummy",
              "package org.jboss.errai.codegen;",
              "@org.jboss.errai.codegen.apt.util.Hook",
              "public class Dummy {",
              "}"));
    if (!compilation.errors().isEmpty()) {
      throw new RuntimeException("Unexpected compilation errors: " + compilation.errors());
    }
  }

}
