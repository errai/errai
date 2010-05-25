package org.jboss.errai.bus.client.api.base;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.RunAsyncCallback;
import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.HasAsyncTaskRef;
import org.jboss.errai.bus.client.api.TaskManager;

public class ClientTaskManager implements TaskManager {
    public void execute(final Runnable task) {
        GWT.runAsync(new RunAsyncCallback() {
            public void onFailure(Throwable reason) {
                GWT.log("failed async execution", reason);
            }

            public void onSuccess() {
                task.run();
            }
        });
    }

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
            boolean cancelled = false;
            public boolean cancel(boolean interrupt) {
                timer.cancel();
                return cancelled = true;
            }

            public void setExitHandler(Runnable runnable) {
                throw new RuntimeException("not implemented");
            }

            public boolean isCancelled() {
                return cancelled;
            }
        };

        if (task instanceof HasAsyncTaskRef) {
            ((HasAsyncTaskRef) task).setAsyncTask(asyncTask);
        }

        return asyncTask;
    }

}
