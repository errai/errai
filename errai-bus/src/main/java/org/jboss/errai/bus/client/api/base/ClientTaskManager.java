package org.jboss.errai.bus.client.api.base;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.client.api.TaskManager;

public class ClientTaskManager implements TaskManager {
    public AsyncTask scheduleRepeating(TimeUnit unit, int interval, final Runnable task) {
        final Timer timer = new Timer() {
            @Override
            public void run() {
                task.run();
            }
        };

        timer.scheduleRepeating((int) unit.convert(interval, TimeUnit.MILLISECONDS));

        return new AsyncTask() {
            public boolean cancel(boolean interrupt) {
                timer.cancel();
                return true;
            }
        };
    }

    public AsyncTask schedule(TimeUnit unit, int interval, final Runnable task) {
        final Timer timer = new Timer() {
            @Override
            public void run() {
                task.run();
            }
        };

        timer.schedule((int) unit.convert(interval, TimeUnit.MILLISECONDS));

        return new AsyncTask() {
            public boolean cancel(boolean interrupt) {
                timer.cancel();
                return true;
            }
        };
    }
}
