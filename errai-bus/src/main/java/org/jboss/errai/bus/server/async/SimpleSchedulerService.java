/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.server.async;

import static java.lang.System.currentTimeMillis;

import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.jboss.errai.bus.client.api.AsyncTask;

/**
 * A basic and efficient scheduler implementation for use by the MessageBus to run housekeeper and other timed
 * tasks.
 *
 * @author Mike Brock
 */
public class SimpleSchedulerService implements Runnable, SchedulerService {
  private volatile boolean running = false;
  private boolean finished = false;

  private long nextRunTime = 0;
  private final TreeSet<TimedTask> tasks = new TreeSet<TimedTask>();
  private final ConcurrentLinkedQueue<TimedTask> toAdd = new ConcurrentLinkedQueue<TimedTask>();

  private boolean autoStartStop = false;
  private Thread currentThread;

  private final Object lock = new Object();

  public SimpleSchedulerService() {
    init();
  }

  private void init() {
    synchronized (lock) {
      if (!running) {
        currentThread = new Thread(this);
        currentThread.setDaemon(true);
        currentThread.setPriority(Thread.MIN_PRIORITY);
      }
    }
  }

  @Override
  public void run() {
    synchronized (lock) {
      running = true;
      finished = false;
    }

    long tm;
    while (running) {
      try {
        while (running) {
          if ((tm = nextRunTime - currentTimeMillis()) > 0) {
            Thread.sleep(tm);
          }

          runAllDue();
        }
      }
      catch (InterruptedException e) {
        if (!running) return;
      }
      catch (ConcurrentModificationException e) {
        // fall-through.
      }
      catch (Throwable t) {
        requestStop();
        throw new RuntimeException("scheduler interrupted by exception", t);
      }
    }

    synchronized (lock) {
      finished = true;
    }
    if (autoStartStop) init();
  }

  @Override
  public void start() {
    currentThread.start();
  }

  public void startIfTasks() {
    synchronized (lock) {
      if (!tasks.isEmpty() && !running) {
        init();
        currentThread.start();
      }
    }
  }

  public void stopIfNoTasks() {
    synchronized (lock) {
      if (running && tasks.isEmpty()) {
        requestStop();
      }
    }
  }

  private void runAllDue() {
    long n = 0;

    synchronized (lock) {
      TimedTask task;
      for (Iterator<TimedTask> iter = tasks.iterator(); iter.hasNext(); ) {
        if ((task = iter.next()).runIfDue(n = currentTimeMillis())) {
          if (task.nextRuntime() == -1) {
            // if the next runtime is -1, that means this event
            // is never scheduled to run again, so we remove it.
            iter.remove();
          }
          else {
            // set the nextRuntime to the nextRuntim of this event
            nextRunTime = task.nextRuntime();
          }
        }
        else if (task.nextRuntime() == -1) {
          // this event is not scheduled to run.
          iter.remove();
        }
        else if (nextRunTime == 0 || task.nextRuntime() < nextRunTime) {
          // this event occurs before the current nextRuntime,
          // so we update nextRuntime.
          nextRunTime = task.nextRuntime();
        }
        else if (n > task.nextRuntime()) {
          // Since the scheduled events are in the order of soonest to
          // latest, we now know that all further events are in the future
          // and we can therefore stop iterating.
          return;
        }
      }

      while (!toAdd.isEmpty()) {
        addTask(toAdd.poll());
      }
      if (autoStartStop) stopIfNoTasks();
    }

    if (n == 0) nextRunTime = currentTimeMillis() + 10000;
  }

  /**
   * Adds a task to be executed. To remove the task, keep a reference to the
   * returned <tt>AsyncTask</tt> and call {@link AsyncTask#cancel(boolean)
   * cancel()} on it.
   *
   * @param task
   */
  @Override
  public AsyncTask addTask(final TimedTask task) {
    synchronized (lock) {
      tasks.add(task);
      if (nextRunTime == 0 || task.nextRuntime() < nextRunTime) {
        nextRunTime = task.nextRuntime();
        currentThread.interrupt();
      }

      if (autoStartStop) startIfTasks();
    }

    return new AsyncTask() {
      private boolean finished = false;

      @Override
      public void cancel(boolean mayInterruptIfRunning) {
        task.cancel(mayInterruptIfRunning);
        finished = true;
      }

      @Override
      public void setExitHandler(Runnable runnable) {

      }

      @Override
      public boolean isCancelled() {
        return finished;
      }
    };
  }

  @Override
  public void addTaskConcurrently(TimedTask task) {
    toAdd.add(task);
  }

  public void setAutoStartStop(boolean autoStartStop) {
    this.autoStartStop = autoStartStop;
  }

  @Override
  public void requestStop() {
    synchronized (lock) {
      currentThread.interrupt();
      running = false;
    }
  }


  public boolean isFinished() {
    synchronized (lock) {
      return finished;
    }
  }

  public void visitAllTasks(TaskVisitor visitor) {
    synchronized (this) {
      for (TimedTask task : tasks) visitor.visit(task);
    }
  }

  public static interface TaskVisitor {
    public void visit(TimedTask task);
  }
}
