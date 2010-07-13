package org.jboss.errai.bus.server.gae;

import com.google.appengine.api.labs.taskqueue.Queue;
import com.google.appengine.api.labs.taskqueue.QueueFactory;
import org.jboss.errai.bus.client.api.AsyncTask;
import org.jboss.errai.bus.server.async.SchedulerService;
import org.jboss.errai.bus.server.async.TimedTask;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.TreeSet;

import static com.google.appengine.api.labs.taskqueue.TaskOptions.Builder.url;
import static java.lang.System.currentTimeMillis;

/**
 * GAE compatible scheduler implementation.
 *
 * @author hbraun
 */
public class GAESchedulerService implements SchedulerService {

    private Logger log = LoggerFactory.getLogger(GAESchedulerService.class);

    private final TreeSet<TimedTask> tasks = new TreeSet<TimedTask>();

    private volatile boolean running = false;
    private boolean finished = false;
    private long nextRunTime = 0;
    private boolean autoStartStop = false;

    public final static GAESchedulerService INSTANCE = new GAESchedulerService();

    public void init() {
        scheduleGAETask();
    }

    private void scheduleGAETask() {
        Queue queue = QueueFactory.getDefaultQueue();
        queue.add(url("/scheduler"));
    }

    public void setAutoStartStop(boolean autoStartStop) {
        this.autoStartStop = autoStartStop;
    }

    public AsyncTask addTask(final TimedTask task) {
        tasks.add(task);

        return new AsyncTask() {
            private boolean finished = false;

            public boolean cancel(boolean mayInterruptIfRunning) {
                task.cancel(mayInterruptIfRunning);
                return finished = true;
            }

            public void setExitHandler(Runnable runnable) {

            }

            public boolean isCancelled() {
                return finished;
            }
        };
    }

    public void runAllDue() {
        long n = 0;

        if ((nextRunTime - currentTimeMillis()) > 0) {
            log.debug("skip execution. next runtime " + nextRunTime);
            return;
        }

        synchronized (this) {
            log.debug("executing scheduler");
            TimedTask task;
            for (Iterator<TimedTask> iter = tasks.iterator(); iter.hasNext();) {
                if ((task = iter.next()).runIfDue(n = currentTimeMillis())) {
                    if (task.nextRuntime() == -1) {
                        // if the next runtime is -1, that means this event
                        // is never scheduled to run again, so we remove it.
                        iter.remove();
                    } else {
                        // set the nextRuntime to the nextRuntim of this event
                        nextRunTime = task.nextRuntime();
                    }
                } else if (task.nextRuntime() == -1) {
                    // this event is not scheduled to run.
                    iter.remove();
                } else if (nextRunTime == 0 || task.nextRuntime() < nextRunTime) {
                    // this event occurs before the current nextRuntime,
                    // so we update nextRuntime.
                    nextRunTime = task.nextRuntime();
                } else if (n > task.nextRuntime()) {
                    // Since the scheduled events are in the order of soonest to
                    // latest, we now know that all further events are in the future
                    // and we can therefore stop iterating.
                    return;
                }
            }

            if (autoStartStop) stopIfNoTasks();
        }

        if (n == 0) nextRunTime = currentTimeMillis() + 10000;
    }

    public void startIfTasks() {
        synchronized (this) {
            if (!tasks.isEmpty() && !running) {
                init();
            }
        }
    }

    public void stopIfNoTasks() {
        synchronized (this) {
            if (running && tasks.isEmpty()) {
                requestStop();
            }
        }
    }

    public void requestStop() {


    }

    public void start() {

    }

}
