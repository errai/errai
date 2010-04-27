package org.jboss.errai.bus.client.api.base;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.HasAsyncTaskRef;
import org.jboss.errai.bus.client.api.TaskManager;

public class ClientTaskManager implements TaskManager {
    public AsyncTask scheduleRepeating(TimeUnit unit, int interval, final Runnable task) {
        final Timer timer = new Timer() {
            @Override
            public void run() {
                task.run();
            }
        };

        AsyncTask asyncTask = createAsyncTask(task, timer);
        timer.scheduleRepeating((int) unit.convert(interval, TimeUnit.MILLISECONDS));
        return asyncTask;
    }

    public AsyncTask schedule(TimeUnit unit, int interval, final Runnable task) {
        final Timer timer = new Timer() {
            @Override
            public void run() {
                task.run();
            }
        };

        AsyncTask asyncTask = createAsyncTask(task, timer);
        timer.schedule((int) unit.convert(interval, TimeUnit.MILLISECONDS));
        return asyncTask;
    }

    private static AsyncTask createAsyncTask(final Runnable task, final Timer timer) {
        AsyncTask asyncTask = new AsyncTask() {
            public boolean cancel(boolean interrupt) {
                timer.cancel();
                return true;
            }
        };

        if (task instanceof HasAsyncTaskRef) {
            ((HasAsyncTaskRef) task).setAsyncTask(asyncTask);
        }

        return asyncTask;
    }

}
