/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.bus.server.async.scheduling;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.QueueOverloadedException;
import org.jboss.errai.bus.server.async.TimedTask;

import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.PriorityBlockingQueue;

import static java.lang.System.currentTimeMillis;

public class PooledExecutorService implements TaskProvider {
    private final ArrayBlockingQueue<TimedTask> queue;

    /**
     * this *must* be an ordered Set. The narrower type is to accomodate wrapping with
     * Collections.synchronizedSet();
     */
    private final BlockingQueue<TimedTask> scheduledTasks;
    //   private final List<TimedTask> fastTaskSchedule;

    private final ThreadWorkerPool pool;

    private volatile int garbageCount = 0;

    private boolean stopped = false;

    private final SchedulerThread schedulerThread;
    private final GarabageCollectorThread garbageCollectorThread;

    private final Object lock = new Object();

    /**
     * Constructs a new DelayedWorkQueue with the specified queue size.
     *
     * @param queueSize The size of the underlying worker queue.
     */
    public PooledExecutorService(int queueSize) {
        queue = new ArrayBlockingQueue<TimedTask>(queueSize);
        pool = new ThreadWorkerPool(this);

        scheduledTasks = new PriorityBlockingQueue<TimedTask>();

        schedulerThread = new SchedulerThread();
        garbageCollectorThread = new GarabageCollectorThread();
    }

    /**
     * Schedule a task for immediate execution.
     *
     * @param runnable Runnable task
     * @throws InterruptedException thrown if the thread waiting for an empty spot on the execution queue is
     *                              interrupted.
     */
    public void execute(final Runnable runnable) throws InterruptedException {
        queue.put(new SingleFireTask(runnable));
    }

    public AsyncTask schedule(final Runnable runnable, TimeUnit unit, long interval) {
        TimedTask task;
        scheduledTasks.add(task = new DelayedTask(runnable, unit.toMillis(interval)));
        return task;
    }

    public AsyncTask scheduleRepeating(final Runnable runnable, final TimeUnit unit, final long initial, final long interval) {
        TimedTask task;
        scheduledTasks.add(task = new RepeatingTimedTask(runnable, unit.toMillis(initial), unit.toMillis(interval)));
        return task;
    }

    public void start() {
        synchronized (lock) {
            if (stopped) {
                throw new IllegalStateException("work queue cannot be started after it's been stopped");
            }

            schedulerThread.start();
            garbageCollectorThread.start();

            pool.startPool();
        }
    }

    public void shutdown() {
        synchronized (lock) {
            schedulerThread.requestStop();
            garbageCollectorThread.requestStop();
            queue.clear();
            stopped = true;
        }
    }

    private long runAllDue() {
        long nextRunTime = 0;

        for (TimedTask task : scheduledTasks) {
            if (task.isDue(currentTimeMillis())) {
                /**
                 * Sechedule the task for execution.
                 */
                task.calculateNextRuntime();
                if (!queue.offer(task)) {
                    throw new QueueOverloadedException("could not schedule task");
                }

                if (task.nextRuntime() == -1) {
                    if (task.isCancelled()) continue;
                    // if the next runtime is -1, that means this event
                    // is never scheduled to run again, so we remove it.
                    task.disable();
                    garbageCount++;
                } else if (nextRunTime == 0 || task.nextRuntime() < nextRunTime) {
                    // set the nextRuntime to the nextRuntim of this event
                    nextRunTime = task.nextRuntime();
                }
            } else if (task.nextRuntime() == -1) {
                if (task.isCancelled()) continue;
                // this event is not scheduled to run.
                task.disable();
                garbageCount++;
            } else if (nextRunTime == 0 || task.nextRuntime() < nextRunTime) {
                // this event occurs before the current nextRuntime,
                // so we update nextRuntime.
                nextRunTime = task.nextRuntime();
            }
        }

        return nextRunTime;
    }

    /**
     * Returns the next Runnable task that is currently due to run.  This method will block until a task is available.
     *
     * @return Runnable task.
     * @throws InterruptedException thrown if the thread waiting on a ready task is interrupted.
     */
    public TimedTask getNextTask() throws InterruptedException {
        return queue.poll(1, java.util.concurrent.TimeUnit.MINUTES);
    }

    private static class SingleFireTask extends TimedTask {
        private final Runnable runnable;
        boolean fired = false;

        private SingleFireTask(Runnable runnable) {
            this.runnable = runnable;
            period = -1;
            nextRuntime = -1;
        }

        @Override
        public boolean isDue(long time) {
            synchronized (this) {
                return !fired;
            }
        }

        public void run() {
            synchronized (this) {
                fired = true;
                runnable.run();
            }
        }
    }

    private static class DelayedTask extends TimedTask {
        private final Runnable runnable;
        private boolean fired = false;

        private DelayedTask(Runnable runnable, long delayMillis) {
            this.runnable = runnable;
            this.nextRuntime = System.currentTimeMillis() + delayMillis;
        }

        @Override
        public boolean isDue(long time) {
            synchronized (this) {
                return !fired && time >= nextRuntime;
            }
        }

        public void run() {
            synchronized (this) {
                fired = true;
                nextRuntime = -1;
                runnable.run();
            }
        }
    }

    private static class RepeatingTimedTask extends TimedTask {
        private final Runnable runnable;

        private RepeatingTimedTask(Runnable runnable, long initialMillis, long intervalMillis) {
            this.runnable = runnable;
            nextRuntime = System.currentTimeMillis() + initialMillis;
            period = intervalMillis;
        }

        public void run() {
            runnable.run();
        }
    }

    private class SchedulerThread extends Thread {
        //private volatile long sleepInterval;
        private volatile boolean running = false;

        private SchedulerThread() {
        }

        @Override
        public void run() {
            while (running) {
                try {
                    while (running) {
                        long tm = runAllDue() - System.currentTimeMillis();
                        if (tm <= 0) tm = 1;
                        sleep(tm <= 0 ? 1 : tm);
                    }
                }
                catch (InterruptedException e) {
                    // just fall through.
                }
            }
        }

        public void start() {
            running = true;
            super.start();
        }

        public void requestStop() {
            running = false;
            interrupt();
        }
    }

    private class GarabageCollectorThread extends Thread {
        private volatile boolean running = false;

        @Override
        public void run() {
            int counter = 0;
            while (running) {
                try {
                    sleep(1000);
                    if (garbageCount != 0) {
                        synchronized (lock) {
                            for (Iterator<TimedTask> iter = scheduledTasks.iterator(); iter.hasNext();) {
                                if (iter.next().isCancelled()) {
                                    iter.remove();
                                    garbageCount--;
                                }
                            }
                        }
                    }
                    if (++counter == 2) {
                        TimedTask task;

                        int i = 0;
                        for (Iterator<TimedTask> iter = scheduledTasks.iterator(); iter.hasNext();) {
                            task = iter.next();
                        }

                        pool.checkLoad();
                        counter = 1;
                    }
                }
                catch (InterruptedException e) {
                    // fall through
                }

            }
        }

        public void start() {
            running = true;
            super.start();
        }

        public void requestStop() {
            running = false;
            interrupt();
        }
    }
}
