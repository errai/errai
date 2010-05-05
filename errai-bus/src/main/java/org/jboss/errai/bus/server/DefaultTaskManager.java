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

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.HasAsyncTaskRef;
import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.async.scheduling.PooledExecutorService;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.lang.System.currentTimeMillis;

public class DefaultTaskManager implements TaskManager {
    private QueueSession session;
    private static final String ACTIVE_TASKS_KEY = DefaultTaskManager.class.getName() + "/ActiveAsyncTasks";

    private final static DefaultTaskManager taskManager = new DefaultTaskManager(null);
    private final static PooledExecutorService service = new PooledExecutorService(50);


    static {
        service.start();
    }

    public static DefaultTaskManager get() {
        return taskManager;
    }

    private DefaultTaskManager(QueueSession session) {
        this.session = session;
    }

    public AsyncTask scheduleRepeating(TimeUnit unit, int interval, Runnable task) {
        AsyncTask t = service.scheduleRepeating(task, unit, 0, interval);

        if (task instanceof HasAsyncTaskRef) {
            ((HasAsyncTaskRef) task).setAsyncTask(t);
        }

        return t;
    }

    public AsyncTask schedule(TimeUnit unit, int interval, Runnable task) {
        AsyncTask t = service.schedule(task, unit, interval);

        if (task instanceof HasAsyncTaskRef) {
            ((HasAsyncTaskRef) task).setAsyncTask(t);
        }

        return t;
    }


    public static void main(String[] args) {
//        taskManager.scheduleRepeating(TimeUnit.MILLISECONDS, 1, new Runnable() {
//            public void run() {
//                try {
//                    Thread.sleep(1);
//                }
//                catch (Exception e) {
//                }
//            }
//        });

        taskManager.scheduleRepeating(TimeUnit.SECONDS, 1, new Runnable() {
            public void run() {
                System.out.println("One Second.");
            }

            @Override
            public String toString() {
                return "One Second";
            }
        });

        taskManager.scheduleRepeating(TimeUnit.SECONDS, 2, new Runnable() {
            public void run() {
                System.out.println("Two Seconds.");
            }

                @Override
            public String toString() {
                return "Two Seconds";
            }

        });

        taskManager.schedule(TimeUnit.SECONDS, 5, new Runnable() {
            public void run() {
                System.out.println("FIVE SECONDS!");
            }

                @Override
            public String toString() {
                return "Five Second";
            }
        });
    }
}
