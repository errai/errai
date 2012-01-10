package org.jboss.errai.bus.client.api;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Abstract test that covers the contract of the AsyncTask interface. Tests for
 * client-side AsyncTask implementations should extend
 * {@link ClientAsyncTaskTest}; tests for server-side implementations should
 * extend this class directly.
 *
 * @author Jonathan Fuerth <jfuerth@gmail.com>
 */
public abstract class AbstractAsyncTaskTest extends GWTTestCase {

  /**
   * A Runnable implementation whose instances keep track of how many times they
   * have been run. Optionally wraps a task that runs whenever {@link #run()} is called.
   */
  public static class CountingRunnable implements Runnable {

    private volatile int runCount = 0;
    private final Runnable task;

    public CountingRunnable() {
      this(null);
    }

    public CountingRunnable(Runnable task) {
      this.task = task;
    }

    @Override
    public void run() {
      try {
        if (task != null) {
          task.run();
        }
      } finally {
        runCount++;
      }
    }

    /**
     * Returns the number of times the {@link #run()} method has been called on
     * this CountingRunnable.
     */
    public int getRunCount() {
      return runCount;
    }
  }

  /**
   * A Runnable that does nothing when run.
   */
  public static final Runnable NO_OP = new Runnable() {
    @Override
    public void run() { };
  };

  /**
   * A Runnable implementation that throws RuntimeException every time it is run.
   */
  public static final Runnable BLOW_UP = new Runnable() {
    @Override
    public void run() { throw new RuntimeException(); };
  };


  /**
   * This version returns null, which forces GWTTestCase to run the tests in
   * "pure java" (not client) mode. If you are testing client-side code, extend
   * {@link ClientAsyncTaskTest} instead of this class.
   */
  @Override
  public String getModuleName() {
    return null;
  }

  /**
   * Returns the AsyncTask implementation that should be tested.
   */
  protected abstract AsyncTask getTaskUnderTest(Runnable task);

  /**
   * Runs the given Runnable after the AsyncTask most recently returned from
   * {@link #getTaskUnderTest(Runnable)} has completed.
   *
   * @param r
   */
  protected abstract void runAfterTaskFinished(Runnable r);

  public void testCancellationFlag() throws Exception {
    AsyncTask task = getTaskUnderTest(NO_OP);
    task.cancel(false);
    assertTrue(task.isCancelled());
  }

  public void testCancellationFlagOnFailingTest() throws Exception {
    final AsyncTask task = getTaskUnderTest(BLOW_UP);
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
    AsyncTask task = getTaskUnderTest(NO_OP);
    task.cancel(false);
    CountingRunnable cr = new CountingRunnable();
    task.setExitHandler(cr);
    assertEquals(1, cr.getRunCount());
  }

  public void testMultipleExitHandlersBeforeTaskFinished() throws Exception {
    AsyncTask task = getTaskUnderTest(NO_OP);

    final CountingRunnable cr1 = new CountingRunnable();
    task.setExitHandler(cr1);
    final CountingRunnable cr2 = new CountingRunnable();
    try {
      task.setExitHandler(cr2);
      fail("Expected IllegalStateException");
    } catch (IllegalStateException e) {
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
    AsyncTask task = getTaskUnderTest(NO_OP);

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
    AsyncTask task = getTaskUnderTest(BLOW_UP);

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

}
