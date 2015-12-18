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

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.common.client.api.tasks.AsyncTask;
import org.jboss.errai.common.client.api.tasks.HasAsyncTaskRef;
import org.jboss.errai.common.client.api.tasks.TaskManager;
import org.jboss.errai.common.client.util.TimeUnit;
import org.jboss.errai.bus.server.async.scheduling.PooledExecutorService;

public class DefaultTaskManager implements TaskManager {
  private QueueSession session;
  private static final String ACTIVE_TASKS_KEY = DefaultTaskManager.class.getName() + "/ActiveAsyncTasks";

  private final static DefaultTaskManager taskManager = new DefaultTaskManager(null);
  private final static PooledExecutorService service = new PooledExecutorService(2000);

  static {
    service.start();
  }

  public static DefaultTaskManager get() {
    return taskManager;
  }

  private DefaultTaskManager(QueueSession session) {
    this.session = session;
  }

  public void execute(Runnable task) {
    try {
      service.execute(task);
    }
    catch (InterruptedException e) {
      throw new RuntimeException(e);
    }
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

  public void requestStop() {
    service.requestStop();
  }
}
