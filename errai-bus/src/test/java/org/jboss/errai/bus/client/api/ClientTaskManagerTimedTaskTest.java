package org.jboss.errai.bus.client.api;

import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.api.tasks.TaskManagerFactory;
import org.jboss.errai.common.client.util.TimeUnit;

import com.google.gwt.user.client.Timer;

public class ClientTaskManagerTimedTaskTest extends ClientAsyncTaskTest {

  private CountingRunnable latestRunnable;

  @Override
  protected AsyncTask getTaskUnderTest(CountingRunnable task) {
    latestRunnable = task;
    assertEquals("Hey, you can't pass me a used runnable!", 0, task.getRunCount());
    return TaskManagerFactory.get().schedule(TimeUnit.MILLISECONDS, 100, latestRunnable);
  }

  @Override
  protected void runAfterTaskFinished(final Runnable r) {
    new Timer() {

      @Override
      public void run() {
        if (latestRunnable.getRunCount() > 0) {
          System.out.println("Task is complete");
          r.run();
        } else {
          System.out.println("Task still hasn't run");
          schedule(100);
        }
      }
    }.schedule(120);
  }
  
  public void testRepeatingTask() throws Exception {
    final CountingRunnable cr = new CountingRunnable();
    final AsyncTask task = TaskManagerFactory.get()
            .scheduleRepeating(TimeUnit.MILLISECONDS, 50, cr);

    // TODO move this to AbstractAsyncTaskTest and make sure it works on both the client and the server
    new Timer() {
      @Override
      public void run() {
        task.cancel(true);
        int actualRunCount = cr.getRunCount();
        int expectedRunCount = 30;
        assertTrue("task executed " + actualRunCount + " times. Should have been at least " + expectedRunCount,
            actualRunCount > expectedRunCount);
        finishTest();
      }
    }.schedule(2000);

    delayTestFinish(5000);
  }
}