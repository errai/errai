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

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.api.QueueSession;
import org.jboss.errai.bus.server.api.SessionEndEvent;
import org.jboss.errai.bus.server.api.SessionEndListener;
import org.jboss.errai.bus.server.api.TaskManager;
import org.jboss.errai.bus.server.async.SchedulerService;
import org.jboss.errai.bus.server.async.ThreadBalancedSchedulerService;
import org.jboss.errai.bus.server.async.TimedTask;

import java.util.concurrent.TimeUnit;

public class DefaultTaskManager implements TaskManager {
    private SchedulerService scheduler;
    private QueueSession session;

    public static DefaultTaskManager getForSession(final QueueSession session) {
        //noinspection SynchronizationOnLocalVariableOrMethodParameter
        synchronized (session) {
            DefaultTaskManager manager = session.getAttribute(DefaultTaskManager.class, TaskManager.class.getName());
            if (manager == null) {
                session.setAttribute(TaskManager.class.getName(), manager = new DefaultTaskManager(session));
            }
            return manager;
        }
    }

    public static DefaultTaskManager getForSession(Message message) {
        return getForSession(message.getResource(QueueSession.class, "Session"));
    }

    private DefaultTaskManager(QueueSession session) {
        scheduler = new ThreadBalancedSchedulerService();
        this.session = session;
        init();
    }

    private void init() {
        session.addSessionEndListener(new SessionEndListener() {
            public void onSessionEnd(SessionEndEvent event) {
                scheduler.requestStop();
            }
        });

        session.setAttribute(TaskManager.class.getName(), this);
    }

    public TimedTask scheduleRepeating(final TimeUnit unit, final int interval, final Runnable task) {
        final TimedTask timedTask = new TimedTask() {
            {
                period = unit.convert(interval, TimeUnit.MILLISECONDS);
            }

            public void run() {
                task.run();
            }
        };
        scheduler.addTask(timedTask);
        return timedTask;
    }


    public TimedTask schedule(final TimeUnit unit, final int interval, final Runnable task) {
        final TimedTask timedTask = new TimedTask() {
            {
                period = unit.convert(interval, TimeUnit.MILLISECONDS);
            }

            public void run() {
                task.run();
                nextRuntime = period = -1; // cancel after first run.
            }
        };
        scheduler.addTask(timedTask);
        return timedTask;
    }
}
