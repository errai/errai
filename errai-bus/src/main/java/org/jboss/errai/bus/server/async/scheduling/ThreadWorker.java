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

import org.jboss.errai.bus.client.api.ErrorCallback;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.async.TimedTask;

import static java.lang.System.nanoTime;

public class ThreadWorker implements Runnable {
    private final Thread thread;
    private final TaskProvider pool;
    private final ErrorCallback errorCallback;


    private volatile double timeSampleStart = 0d;
    private volatile double cpuTime = 0;
    private volatile double avgLoad = 0;

    private static final long SEC10 = TimeUnit.NANOSECONDS.convert(10, TimeUnit.SECONDS);

    private volatile boolean active = false;

    public ThreadWorker(TaskProvider pool) {
        this.thread = new Thread(this);
        this.pool = pool;
        this.errorCallback = null;
    }

    public ThreadWorker(ErrorCallback errorCallback, TaskProvider pool) {
        this.thread = new Thread(this);
        this.errorCallback = errorCallback;
        this.pool = pool;
    }

    public void start() {
        // Start sampling the load in the past, to simplify calculations.
        timeSampleStart = nanoTime() - SEC10;
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

    public void run() {
        while (active) {
            try {
                while (active) {
                    timeSampleStart = nanoTime();
                    TimedTask task = pool.getNextTask();

                    if (task == null) {
                        continue;
                    }

                    long tm = nanoTime();
                    task.runNow();
                    cpuTime = (nanoTime() - tm);

                    calculateLoad();
                }
            }
            catch (InterruptedException e) {
                e.printStackTrace();
                if (!active) {
                    /**
                     * If the thread has been marked inactive, terminate now.  Otherwise continue along
                     * normally.
                     */
                    return;
                }
            }
            catch (Throwable t) {
                t.printStackTrace();
                if (errorCallback != null) {
                    /**
                     * If the errorCallback is defined for this ThreadWorker, we report the exception we
                     * just experienced.
                     */
                    errorCallback.error(null, t);
                }
            }
        }
    }


    private void calculateLoad() {
       avgLoad = cpuTime / (nanoTime() - timeSampleStart);
    }

    public double getApparentLoad() {
        return avgLoad;
    }
}
