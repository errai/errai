package org.jboss.errai.bus.client.api;

import org.jboss.errai.bus.client.api.base.TaskManagerFactory;
import org.jboss.errai.bus.client.api.base.TimeUnit;

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
}
