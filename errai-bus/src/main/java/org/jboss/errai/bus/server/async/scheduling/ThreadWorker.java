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

package org.jboss.errai.bus.server.async.scheduling;

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.bus.server.async.TimedTask;

public class ThreadWorker implements Runnable {
  private final Thread thread;
  private final TaskProvider pool;
  private final ErrorCallback errorCallback;

  private volatile boolean active = false;
  private volatile boolean isStopped = false;

  public ThreadWorker(TaskProvider pool) {
    this.thread = new Thread(this, "ExecutorPoolWorker");
    this.thread.setDaemon(true);
    this.pool = pool;
    this.errorCallback = null;
  }

  public ThreadWorker(ErrorCallback errorCallback, TaskProvider pool) {
    this.thread = new Thread(this);
    this.thread.setDaemon(true);
    this.errorCallback = errorCallback;
    this.pool = pool;
  }

  public void start() {
    // Start sampling the load in the past, to simplify calculations.
    active = true;
    thread.start();
  }

  public void requestStop() {
    active = false;
    thread.interrupt();
  }

  public boolean isAlive() {
    return !thread.isAlive();
  }

  @Override
  public void run() {
    TimedTask task = null;
    while (active) {
      try {
        while (active) {
          if ((task = pool.getNextTask()) == null) {
            continue;
          }
          task.run();
        }
      }
      catch (InterruptedException e) {
        if (!active) {
          /**
           * If the thread has been marked inactive, terminate now.  Otherwise continue along
           * normally.
           */
          isStopped = true;
          return;
        }
      }
      catch (Throwable t) {
        if (task != null) {
          task.cancel(true);
        }

        if (errorCallback != null) {
          /**
           * If the errorCallback is defined for this ThreadWorker, we report the exception we
           * just experienced.
           */
          errorCallback.error(null, t);
        }
        else {
          t.printStackTrace();
        }
      }
    }
    isStopped = true;
  }

  public Thread.State getThreadState() {
    return thread.getState();
  }

  public boolean isStopped() {
    return isStopped;
  }
}
