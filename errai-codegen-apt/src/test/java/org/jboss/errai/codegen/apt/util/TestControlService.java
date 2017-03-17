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

import java.util.Iterator;

import org.junit.runner.Description;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunNotifier;

/**
 * Service for communicating between {@link InProcessorTestRunner} and {@link UnitTestRunningAnntoationProcessor}.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class TestControlService {

  private final RunNotifier notifier;
  private final Iterator<Description> tests;
  private Description currentlyRunning;
  private final String testClassName;

  public TestControlService(final RunNotifier notifier, final Iterator<Description> tests, final String testClassName) {
    this.notifier = notifier;
    this.tests = tests;
    this.testClassName = testClassName;
  }

  public String getTestClassName() {
    return testClassName;
  }

  public String requestNextTest() {
    if (currentlyRunning != null) {
      throw new IllegalStateException(String.format("Must resolve currently pending test [%s].", currentlyRunning));
    }

    if (tests.hasNext()) {
      final Description next = tests.next();
      notifier.fireTestStarted(next);
      currentlyRunning = next;

      return next.getMethodName();
    }
    else {
      return null;
    }
  }

  public void submitSuccess() {
    assertCurrentlyRunningTest();
    notifier.fireTestFinished(currentlyRunning);
    currentlyRunning = null;
  }

  public void submitFailure(final Throwable t) {
    assertCurrentlyRunningTest();
    notifier.fireTestFailure(new Failure(currentlyRunning, t));
    currentlyRunning = null;
  }

  private void assertCurrentlyRunningTest() {
    if (currentlyRunning == null) {
      throw new IllegalStateException("Cannot submit result when no test is currently running.");
    }
  }

}
