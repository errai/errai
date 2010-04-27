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
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.TaskManager;
import org.jboss.errai.bus.client.api.base.TimeUnit;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.api.SessionEndEvent;
import org.jboss.errai.bus.server.api.SessionEndListener;
import org.jboss.errai.bus.server.async.PooledSchedulerService;
import org.jboss.errai.bus.server.async.SchedulerService;
import org.jboss.errai.bus.server.async.TimedTask;
import org.jboss.errai.bus.server.util.LocalContext;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.RunnableScheduledFuture;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import static java.lang.System.currentTimeMillis;

public class DefaultTaskManager implements TaskManager {
    private QueueSession session;
    private static final String ACTIVE_TASKS_KEY = DefaultTaskManager.class.getName() + "/ActiveAsyncTasks";

    private final static ScheduledThreadPoolExecutor executor;
    private final static DefaultTaskManager taskManager = new DefaultTaskManager(null);

    static {
        executor = new ScheduledThreadPoolExecutor(30);
    }

    public static DefaultTaskManager get() {
        return taskManager;
    }


    private DefaultTaskManager(QueueSession session) {
        this.session = session;
    }

    public AsyncTask scheduleRepeating(final TimeUnit unit, final int interval, final Runnable task) {
        long itv = unit.convert(interval, TimeUnit.MILLISECONDS);
        return createAsyncTask(executor.scheduleAtFixedRate(task, itv, itv, java.util.concurrent.TimeUnit.MILLISECONDS), task);
            }

    public AsyncTask schedule(final TimeUnit unit, final int interval, final Runnable task) {
        long itv = unit.convert(interval, TimeUnit.MILLISECONDS);
        return createAsyncTask(executor.schedule(task, itv - currentTimeMillis(), java.util.concurrent.TimeUnit.MILLISECONDS), task);

    }

    private AsyncTask createAsyncTask(final ScheduledFuture future, final Runnable task) {
        final AsyncTask asyncTask = new AsyncTask() {
            public boolean cancel(boolean interrupt) {
                return future.cancel(interrupt);
            }
        };

        if (task instanceof HasAsyncTaskRef) {
            ((HasAsyncTaskRef) task).setAsyncTask(asyncTask);
        }


        return asyncTask;
    }
}
