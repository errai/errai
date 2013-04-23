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

/**
 * @author Mike Brock
 */
public class ManyTimesTestRunner extends BlockJUnit4ClassRunner {
  public ManyTimesTestRunner(final Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected Description describeChild(final FrameworkMethod method) {
    if (method.getAnnotation(Ignore.class) == null) {
      return describeRepeatTest(method);
    }
    return super.describeChild(method);
  }

  private Description describeRepeatTest(final FrameworkMethod method) {
    final Description description = Description.createSuiteDescription(
        testName(method));

    final Class<?> javaClass = getTestClass().getJavaClass();
    final String testMethod = testName(method);

    for (int i = 1; i < 1000; i++) {
      description.addChild(Description.createTestDescription(javaClass, "[" + i + "] " + testMethod));
    }
    return description;
  }

  @Override
  protected void runChild(final FrameworkMethod method, final RunNotifier notifier) {
    for (final Description child : describeChild(method).getChildren()) {
      System.out.println("<<RUNNING:" + child.getDisplayName() +">>");
      runLeafNode(method, notifier, child);
      System.out.println("<<FINISHED:" + child.getDisplayName() +">>");
    }

     super.runChild(method, notifier);
  }

  protected void runLeafNode(final FrameworkMethod method, final RunNotifier notifier, final Description description) {
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
      eachNotifier.addFailure(e);
    }
    finally {
      eachNotifier.fireTestFinished();
    }
  }
}
