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
import org.junit.runners.model.Statement;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class ManyTimesTestRunner extends BlockJUnit4ClassRunner {
  public ManyTimesTestRunner(Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected List<FrameworkMethod> getChildren() {
    return super.getChildren();
  }

  @Override
  protected Description describeChild(FrameworkMethod method) {
    if (method.getAnnotation(Ignore.class) == null) {
      return describeRepeatTest(method);
    }
    return super.describeChild(method);
  }

  private Description describeRepeatTest(FrameworkMethod method) {
    //   int times = method.getAnnotation(Repeat.class).value();

    Description description = Description.createSuiteDescription(
        testName(method) + " many times",
        method.getAnnotations());

    for (int i = 1; i < 1000; i++) {
      description.addChild(Description.createTestDescription(
          getTestClass().getJavaClass(),
          "[" + i + "] " + testName(method)));
    }
    return description;
  }

  @Override
  protected void runChild(final FrameworkMethod method, RunNotifier notifier) {
    Description description = describeChild(method);
    final ArrayList<Description> children = description.getChildren();
    for (Description child : children) {
      runLeafNode(method, notifier, child);
    }

    super.runChild(method, notifier);
  }

  protected void runLeafNode(FrameworkMethod method, RunNotifier notifier, Description description) {
    EachTestNotifier eachNotifier = new EachTestNotifier(notifier, description);
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

  private void runRepeatedly(Statement statement, Description description, RunNotifier notifier) {


    //  for (Description desc : description.getChildren()) {

    //   runLeaf(statement, desc, notifier);
    //  }
  }
}
