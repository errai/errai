/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.async;

import static java.lang.System.currentTimeMillis;

import org.jboss.errai.common.client.api.tasks.AsyncTask;

/**
 * A <tt>TimedTask</tt> is used for scheduling tasks, and making sure they are run at appropriate times and intervals
 */
public abstract class TimedTask implements Runnable, Comparable<TimedTask>, AsyncTask {
  protected volatile long nextRuntime;
  protected volatile long period;
  protected volatile boolean cancelled = false;

  protected volatile InterruptHandle interruptHook;
  protected volatile Runnable exitHandler;

  /**
   * Gets the period of the task, and when it should be run next
   *
   * @return the interval length
   */
  public long getPeriod() {
    return period;
  }

  /**
   * Sets the period in which the task should be run next
   *
   * @param period
   */
  public void setPeriod(long period) {
    this.period = period;
  }


  public void cancel() {
    period = -1;
  }

  /**
   * Gets the time in which the task will be run.  If -1 is returned, the task is permanently de-scheduled.
   *
   * @return the time the task will be run in milliseconds
   */
  public long nextRuntime() {
    return nextRuntime;
  }

  @Override
  public void cancel(boolean interrupt) {
    if (interrupt && interruptHook != null)
      interruptHook.sendInterrupt();

    cancelled = true;
  }

  /**
   * Returns true if the task has been cancelled or is expired.
   *
   * @return
   */
  @Override
  public boolean isCancelled() {
    return cancelled;
  }

  public boolean calculateNextRuntime() {
    synchronized (this) {
      if (!cancelled && period != -1) {
        nextRuntime = currentTimeMillis() + period;
        return true;
      }
      else {
        nextRuntime = -1;
        return false;
      }
    }
  }


  public boolean isDue(long time) {
    synchronized (this) {
      return !cancelled && nextRuntime <= time && nextRuntime != -1;
    }
  }

  @Override
  public void setExitHandler(Runnable runnable) {
    if (exitHandler != null) {
      throw new IllegalStateException("Exit handler already set to " + exitHandler);
    }
    this.exitHandler = runnable;
    if (!isDue(currentTimeMillis())) {
      runnable.run();
    }
  }

  @Override
  public int compareTo(TimedTask o) {
    if (o == this) {
      return 0;
    }
    if (nextRuntime > o.nextRuntime)
      return 1;
    else if (nextRuntime < o.nextRuntime)
      return -1;
    else
      return 0;
  }
}
