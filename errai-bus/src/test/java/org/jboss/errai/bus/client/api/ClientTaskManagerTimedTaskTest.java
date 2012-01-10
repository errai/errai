package org.jboss.errai.bus.client.api;

import org.jboss.errai.bus.client.api.base.TaskManagerFactory;
import org.jboss.errai.bus.client.api.base.TimeUnit;

import com.google.gwt.user.client.Timer;

public class ClientTaskManagerTimedTaskTest extends ClientAsyncTaskTest {

  private CountingRunnable latestRunnable;

  @Override
  protected AsyncTask getTaskUnderTest(Runnable task) {
    latestRunnable = new CountingRunnable(task);
    return TaskManagerFactory.get().schedule(TimeUnit.MILLISECONDS, 100, latestRunnable);
  }

  @Override
  protected void runAfterTaskFinished(final Runnable r) {
    new Timer() {

      @Override
      public void run() {
        if (latestRunnable.getRunCount() > 0) {
          r.run();
        } else {
          schedule(100);
        }
      }
    }.schedule(120);
  }
}
