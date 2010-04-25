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

package org.jboss.errai.bus.server.async;


import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class PooledSchedulerService implements SchedulerService {
    private final static ScheduledThreadPoolExecutor executor;

    static {
        executor = new ScheduledThreadPoolExecutor(30);
    }

    public PooledSchedulerService() {
    }

    public ScheduledFuture addTask(TimedTask task) {
        if (task.period == -1) {
            return executor.schedule(task, task.nextRuntime() - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
        else {
            return executor.scheduleAtFixedRate(task, task.getPeriod(), task.getPeriod(), TimeUnit.MILLISECONDS);
        }
    }

    public void requestStop() {
        // not supported
    }

    public void start() {
        //
    }
}
