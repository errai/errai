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

package org.jboss.errai.bus.client.api;

import com.google.gwt.junit.client.GWTTestCase;
import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.api.tasks.HasAsyncTaskRef;

/**
 * Abstract test that covers the contract of the AsyncTask interface. Tests for client-side AsyncTask implementations
 * should extend {@link ClientAsyncTaskTest}; tests for server-side implementations should extend this class directly.
 * 
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class AbstractAsyncTaskTest extends GWTTestCase {

  /**
   * A Runnable implementation whose instances keep track of how many times they have been run. Optionally wraps a task
   * that runs whenever {@link #run()} is called. Also implements {@link HasAsyncTaskRef} to make it easier to verify
   * injection of AsyncTask.
   */
  public static class CountingRunnable implements Runnable, HasAsyncTaskRef {

    private volatile int runCount = 0;
    private final Runnable wrappedRunnable;
    private AsyncTask injectedAsyncTask;

    public CountingRunnable() {
      this(null);
    }

    public CountingRunnable(Runnable task) {
      this.wrappedRunnable = task;
    }

    @Override
    public void run() {
      try {
        if (wrappedRunnable != null) {
          wrappedRunnable.run();
        }
      }
      finally {
        runCount++;
      }
    }

    /**
     * Returns the number of times the {@link #run()} method has been called on this CountingRunnable.
     */
    public int getRunCount() {
      return runCount;
    }

    @Override
    public void setAsyncTask(AsyncTask task) {
      this.injectedAsyncTask = task;
    }

    @Override
    public AsyncTask getAsyncTask() {
      return injectedAsyncTask;
    }

  }

  /**
   * A Runnable implementation that throws RuntimeException every time it is run.
   */
  public static final Runnable BLOW_UP = new Runnable() {
    @Override
    public void run() {
      throw new RuntimeException("This exception is intentionally thrown as part of a test.");
    };
  };

  /**
   * This version returns null, which forces GWTTestCase to run the tests in "pure java" (not client) mode. If you are
   * testing client-side code, extend {@link ClientAsyncTaskTest} instead of this class.
   */
  @Override
  public String getModuleName() {
    return null;
  }

  /**
   * Returns the AsyncTask implementation that should be tested.
   * 
   * @param task
   *          the Runnable to pass to the execution system. <b>Note to subclassers: this runnable must be passed as-is,
   *          without wrapping.</b> Tests such as {@link #testAsyncTaskRefInjection} depend on it.
   */
  protected abstract AsyncTask getTaskUnderTest(CountingRunnable task);

  /**
   * Runs the given Runnable after the AsyncTask most recently returned from {@link #getTaskUnderTest(CountingRunnable)}
   * has completed.
   * 
   * @param r
   */
  protected abstract void runAfterTaskFinished(Runnable r);

  public void testCancellationFlag() throws Exception {
    AsyncTask task = getTaskUnderTest(new CountingRunnable());
    task.cancel(false);
    assertTrue(task.isCancelled());
  }

  public void testCancellationFlagOnFailingTest() throws Exception {
    final AsyncTask task = getTaskUnderTest(new CountingRunnable(BLOW_UP));
    runAfterTaskFinished(new Runnable() {

      @Override
      public void run() {
        assertTrue(task.isCancelled());
        finishTest();
      }
    });
    delayTestFinish(5000);
  }

  public void testExitHandlerRunsImmediatelyOnCancelledTask() throws Exception {
    AsyncTask task = getTaskUnderTest(new CountingRunnable());
    task.cancel(false);
    CountingRunnable cr = new CountingRunnable();
    task.setExitHandler(cr);
    assertEquals(1, cr.getRunCount());
  }

  public void testMultipleExitHandlersBeforeTaskFinished() throws Exception {
    AsyncTask task = getTaskUnderTest(new CountingRunnable());

    final CountingRunnable cr1 = new CountingRunnable();
    task.setExitHandler(cr1);
    final CountingRunnable cr2 = new CountingRunnable();
    try {
      task.setExitHandler(cr2);
      fail("Expected IllegalStateException");
    }
    catch (IllegalStateException e) {
      // expected
    }

    runAfterTaskFinished(new Runnable() {
      @Override
      public void run() {
        assertEquals(1, cr1.getRunCount());
        assertEquals(0, cr2.getRunCount());
        finishTest();
      }
    });
    delayTestFinish(5000);
  }

  public void testAddExitHandlerBeforeTaskFinished() throws Exception {
    AsyncTask task = getTaskUnderTest(new CountingRunnable());

    final CountingRunnable cr = new CountingRunnable();
    task.setExitHandler(cr);

    runAfterTaskFinished(new Runnable() {
      @Override
      public void run() {
        assertEquals(1, cr.getRunCount());
        finishTest();
      }
    });
    delayTestFinish(5000);
  }

  public void testAddExitHandlerForFailingTask() throws Exception {
    AsyncTask task = getTaskUnderTest(new CountingRunnable(BLOW_UP));

    final CountingRunnable cr = new CountingRunnable();
    task.setExitHandler(cr);

    runAfterTaskFinished(new Runnable() {
      @Override
      public void run() {
        assertEquals(1, cr.getRunCount());
        finishTest();
      }
    });
    delayTestFinish(5000);
  }

  public void testIsFinishedAfterTaskFinishes() throws Exception {
    final CountingRunnable countingRunnable = new CountingRunnable();
    final AsyncTask task = getTaskUnderTest(countingRunnable);

    runAfterTaskFinished(new Runnable() {
      @Override
      public void run() {
        assertEquals(countingRunnable.runCount, 1);
        assertTrue(task.isFinished());
        finishTest();
      }
    });
    delayTestFinish(5000);
  }

  public void testIsFinishedAfterTaskCancelled() throws Exception {
    final CountingRunnable countingRunnable = new CountingRunnable();
    final AsyncTask task = getTaskUnderTest(countingRunnable);

    task.cancel(false);

    assertEquals(countingRunnable.runCount, 0);
    assertTrue(task.isFinished());
  }

  public void testIsFinishedAfterTaskFailure() throws Exception {
    final AsyncTask task = getTaskUnderTest(new CountingRunnable(BLOW_UP));

    runAfterTaskFinished(new Runnable() {
      @Override
      public void run() {
        assertTrue(task.isFinished());
        finishTest();
      }
    });
    delayTestFinish(5000);
  }

  public void testAsyncTaskRefInjection() throws Exception {
    CountingRunnable runnable = new CountingRunnable();
    AsyncTask task = getTaskUnderTest(runnable);
    assertNotNull("No AsyncTask was injected into runnable", runnable.getAsyncTask());
    assertSame("Wrong AsyncTask injected into runnable", task, runnable.getAsyncTask());
  }
}

