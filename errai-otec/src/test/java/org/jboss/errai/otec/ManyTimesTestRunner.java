/*
 * Copyright 2013 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.otec;

import org.junit.Ignore;
import org.junit.internal.AssumptionViolatedException;
import org.junit.internal.runners.model.EachTestNotifier;
import org.junit.runner.Description;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class ManyTimesTestRunner extends BlockJUnit4ClassRunner {
  public ManyTimesTestRunner(final Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected Description describeChild(final FrameworkMethod method) {
    if (method.getAnnotation(Ignore.class) == null && method.getAnnotation(NoFuzz.class) == null) {
      return describeRepeatTest(method);
    }
    return super.describeChild(method);
  }

  private Description describeRepeatTest(final FrameworkMethod method) {
    final Description description = Description.createSuiteDescription(
        testName(method));

    final Class<?> javaClass = getTestClass().getJavaClass();
    final String testMethod = testName(method);

    final int runCount = 2500;
    for (int i = 1; i < runCount; i++) {
      description.addChild(Description.createTestDescription(javaClass, "[" + i + "] " + testMethod));
    }
    return description;
  }

  @Override
  protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
    if (method.getAnnotation(NoFuzz.class) != null) {
      notifier.fireTestIgnored(describeChild(method));
      return;
    }

    final PrintStream oldPrintOut = System.out;
    try {

      Set<String> variationsTested = new HashSet<String>();

      for (final Description child : describeChild(method).getChildren()) {
        final ByteArrayOutputStream byteStream = new ByteArrayOutputStream(1024 * 1024);
        System.setOut(new PrintStream(byteStream));
        runLeafNode(method, notifier, child, byteStream, oldPrintOut, variationsTested);
      }

      oldPrintOut.println("Tested " + variationsTested.size() + " unique variations of: " + describeChild(method));
//
//      for (String v : variationsTested) {
//        oldPrintOut.println(v);
//      }

      super.runChild(method, notifier);
    }
    finally {
      System.setOut(oldPrintOut);
    }
  }

  protected void runLeafNode(final FrameworkMethod method,
                             final RunNotifier notifier,
                             final Description description,
                             final ByteArrayOutputStream outputBucket,
                             final PrintStream originalPrintOut,
                             final Set<String> variations) {
    final EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
    if (method.getAnnotation(Ignore.class) != null) {
      eachNotifier.fireTestIgnored();
      return;
    }
    eachNotifier.fireTestStarted();
    try {
      methodBlock(method).evaluate();
    }
    catch (AssumptionViolatedException e) {
      eachNotifier.addFailedAssumption(e);
    }
    catch (Throwable e) {
      originalPrintOut.println("FAILURE REPORT:\n\n");
      originalPrintOut.println(new String(outputBucket.toByteArray()));
      eachNotifier.addFailure(e);
    }
    finally {
      variations.add(new String(outputBucket.toByteArray()).trim());
      eachNotifier.fireTestFinished();
    }
  }
}
