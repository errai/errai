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
