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

import static java.lang.System.currentTimeMillis;
import static java.util.concurrent.locks.LockSupport.parkUntil;
import static org.slf4j.LoggerFactory.getLogger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.errai.bus.server.async.InterruptHandle;
import org.jboss.errai.bus.server.async.TimedTask;
import org.jboss.errai.bus.server.service.ErraiConfigAttribs;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.api.tasks.HasAsyncTaskRef;
import org.jboss.errai.common.client.util.TimeUnit;
import org.slf4j.Logger;

public class PooledExecutorService implements TaskProvider {
  private final static Logger log = getLogger(PooledExecutorService.class);
  private final BlockingQueue<TimedTask> queue;

  /**
   * this *must* be an ordered Set. The narrower type is to accomodate wrapping with
   * Collections.synchronizedSet();
   */
  private final BlockingQueue<TimedTask> scheduledTasks;
  private final ThreadWorkerPool pool;

  private boolean stopped = false;

  private final SchedulerThread schedulerThread;

  private final ReentrantLock mutex = new ReentrantLock(true);
  private final int maxQueueSize;

  private final SaturationPolicy saturationPolicy;

  /**
   * Enumeration of possible ways of handling a queue full scenario.
   */
  public enum SaturationPolicy {

    /**
     * Runs the task in the calling thread.
     */
    CallerRuns {
      @Override
      void dealWith(Runnable task) {
        task.run();
      }
    },

    /**
     * Throws a RuntimeException when called. The exception message includes
     * {@code task.toString()}, so name your runnables if you'd like nice messages
     * in this case.
     */
    Fail {
      @Override
      void dealWith(Runnable task) {
        throw new RuntimeException("queue is saturated. not running " + task);
      }
    };

    /**
     * Deals with the given task in the manner consistent with the
     * SaturationPolicy in use. See the documentation of the individual
     * saturation policies for details.
     */
    abstract void dealWith(Runnable task);
  }

  /**
   * Constructs a new PooledExecutorService with the specified queue size.
   *
   * @param queueSize The size of the underlying worker queue.
   */
  public PooledExecutorService(int queueSize) {
    this(queueSize, SaturationPolicy.valueOf(
            ErraiConfigAttribs.SATURATION_POLICY.get(new ErraiServiceConfiguratorImpl())));
  }

  public PooledExecutorService(int queueSize, SaturationPolicy saturationPolicy) {
    maxQueueSize = queueSize;
    queue = new ArrayBlockingQueue<TimedTask>(queueSize);
    pool = new ThreadWorkerPool(this);

    scheduledTasks = new PriorityBlockingQueue<TimedTask>();
    schedulerThread = new SchedulerThread();
    this.saturationPolicy = saturationPolicy;
    
  }

  /**
   * Schedule a task for immediate execution.
   *
   * @param runnable Runnable task
   * @throws InterruptedException thrown if the thread waiting for an empty spot on the execution queue is
   *                              interrupted.
   */
  public void execute(final Runnable runnable) throws InterruptedException {
    checkLoad();
    if (!queue.offer(new SingleFireTask(runnable))) {
      saturationPolicy.dealWith(runnable);
    }

    //    queue.add(new SingleFireTask(runnable));
  }

  public AsyncTask schedule(final Runnable runnable, TimeUnit unit, long interval) {
    checkLoad();
    mutex.lock();
    try {
      TimedTask task;
      scheduledTasks.offer(task = new DelayedTask(runnable, unit.toMillis(interval)));

      return task;
    }
    finally {
      mutex.unlock();
    }
  }

  public AsyncTask scheduleRepeating(final Runnable runnable, final TimeUnit unit, final long initial, final long interval) {
    checkLoad();
    mutex.lock();
    try {
      TimedTask task;
      scheduledTasks.offer(task = new RepeatingTimedTask(runnable, unit.toMillis(initial), unit.toMillis(interval)));

      return task;
    }
    finally {
      mutex.unlock();
    }
  }

  public void start() {
    mutex.lock();
    try {
      if (stopped) {
        throw new IllegalStateException("work queue cannot be started after it's been stopped");
      }

      schedulerThread.start();

      pool.startPool();
    }
    finally {
      mutex.unlock();
    }
  }

  public void shutdown() {
    mutex.lock();
    try {
      schedulerThread.requestStop();
      queue.clear();
      stopped = true;
    }
    finally {
      mutex.unlock();
    }
  }

  private long runAllDue() throws InterruptedException {
    long nextRunTime = 0;
    TimedTask task;

    while ((task = scheduledTasks.poll(60, java.util.concurrent.TimeUnit.SECONDS)) != null) {
      if (!task.isDue(currentTimeMillis())) {
        parkUntil(task.nextRuntime());
      }

      /**
       * Sechedule the task for execution.
       */
      if (!queue.offer(task, 5, java.util.concurrent.TimeUnit.SECONDS)) {
        saturationPolicy.dealWith(task);
      }

      if (task.calculateNextRuntime()) {
        scheduledTasks.offer(task);
      }
    }

    return nextRunTime;

  }

  private volatile int idleCount = 0;

  private void checkLoad() {
    int queueSize = queue.size();

    if (queueSize == 0) return;
    else if (queueSize > (0.80d * maxQueueSize)) {
      pool.addWorker();
    }

    if (idleCount > 100) {
      idleCount = 0;
      pool.removeWorker();
    }
  }

  /**
   * Returns the next Runnable task that is currently due to run.  This method will block until a task is available.
   *
   * @return Runnable task.
   * @throws InterruptedException thrown if the thread waiting on a ready task is interrupted.
   */
  @Override
  public TimedTask getNextTask() throws InterruptedException {
    if (queue != null)
      return queue.poll(1, java.util.concurrent.TimeUnit.SECONDS);
    else
      return null; // It's yet unclear how this happens. See https://jira.jboss.org/browse/ERRAI-104
  }

  private static class SingleFireTask extends TimedTask {
    private final Runnable runnable;
    boolean fired = false;

    private SingleFireTask(Runnable runnable) {
      period = -1;
      nextRuntime = -1;
      this.runnable = runnable;

      // this must come last, and SingleFireTask must not be subclassed,
      // lest we leak a ref to a partly constructed object.
      if (runnable instanceof HasAsyncTaskRef) {
        ((HasAsyncTaskRef) runnable).setAsyncTask(this);
      }
    }

    @Override
    public boolean isDue(long time) {
      synchronized (this) {
        return !fired;
      }
    }

    @Override
    public boolean isFinished() {
      return fired || isCancelled();
    }

    @Override
    public void run() {
      synchronized (this) {
        fired = true;
        runnable.run();
      }
    }
  }

  private static final class DelayedTask extends TimedTask {
    private final Runnable runnable;
    private boolean fired = false;
    private volatile Thread runningOn;

    private DelayedTask(Runnable runnable, long delayMillis) {
      this.interruptHook = new InterruptHandle() {
        @Override
        public void sendInterrupt() {
          try {
            if (runningOn != null)
              runningOn.interrupt();
          }
          catch (NullPointerException e) {
            // if runningOn is de-referenced, it means the task has completed
            // and no interrupt is necessary, so we ignore this exception.
          }
        }
      };
      this.period = -1;
      this.nextRuntime = System.currentTimeMillis() + delayMillis;
      this.runnable = runnable;

      // this must come last, and DelayedTask must not be subclassed,
      // lest we leak a ref to a partly constructed object.
      if (runnable instanceof HasAsyncTaskRef) {
        ((HasAsyncTaskRef) runnable).setAsyncTask(this);
      }
    }

    @Override
    public boolean isDue(long time) {
      synchronized (this) {
        return !fired && time >= nextRuntime;
      }
    }

    @Override
    public boolean isFinished() {
      return fired || isCancelled();
    }

    @Override
    public void run() {
      synchronized (this) {
        fired = true;
        nextRuntime = -1;
        try {
          runningOn = Thread.currentThread();
          runnable.run();
        }
        finally {
          runningOn = null;
          if (exitHandler != null)
            exitHandler.run();
        }
      }
    }
  }

  private static final class RepeatingTimedTask extends TimedTask {
    private final Runnable runnable;
    private volatile Thread runningOn;
    private volatile boolean finished;

    private RepeatingTimedTask(Runnable runnable, long initialMillis, long intervalMillis) {
      this.interruptHook = new InterruptHandle() {
        @Override
        public void sendInterrupt() {
          try {
            if (runningOn != null)
              runningOn.interrupt();
          }
          catch (NullPointerException e) {
            // if runningOn is de-referenced, it means the task has completed
            // and no interrupt is necessary, so we ignore this exception.
          }
        }
      };
      nextRuntime = System.currentTimeMillis() + initialMillis;
      period = intervalMillis;
      this.runnable = runnable;

      // this must come last, and RepeatingTimedTask must not be subclassed,
      // lest we leak a ref to a partly constructed object.
      if (runnable instanceof HasAsyncTaskRef) {
        ((HasAsyncTaskRef) runnable).setAsyncTask(this);
      }
    }

    @Override
    public boolean isFinished() {
      return finished || isCancelled();
    }

    @Override
    public void run() {
      try {
        runningOn = Thread.currentThread();
        runnable.run();
      }
      finally {
        runningOn = null;
        if ((cancelled || nextRuntime == -1) && exitHandler != null) {
          exitHandler.run();
          finished = true;
        }
      }
    }
  }

  private class SchedulerThread extends Thread {
    private volatile boolean running = false;

    private SchedulerThread() {
    }

    @Override
    public void run() {
      while (running) {
        try {
          while (running) {
            runAllDue();
          }
        }
        catch (InterruptedException e) {
          // This will happen during container shutdown, so logging as debug is sufficient.
          log.debug("Scheduler thread interrupted", e);
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    }

    @Override
    public void start() {
      running = true;
      super.start();
    }

    public void requestStop() {
      running = false;
      interrupt();
    }
  }

  public void requestStop() {
    stopped = true;
    pool.requestStopAll();
    schedulerThread.requestStop();
  }

}
