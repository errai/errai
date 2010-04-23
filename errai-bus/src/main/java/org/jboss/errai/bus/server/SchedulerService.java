package org.jboss.errai.bus.server;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

import static java.lang.System.currentTimeMillis;

/**
 * A basic and efficient scheduler implementation for use by the MessageBus to run housekeeper and other timed
 * tasks.
 *
 * @author Mike Brock
 */
public class SchedulerService implements Runnable {
    private volatile boolean running = false;
    private long nextRunTime = 0;
    private final TreeSet<TimedTask> tasks = new TreeSet<TimedTask>();
    private boolean autoStartStop = false;
    private Thread currentThread;

    public SchedulerService() {
        init();
    }

    private void init() {
        synchronized (this) {
            if (!running) {
                currentThread = new Thread(this);
                currentThread.setPriority(Thread.MIN_PRIORITY);
            }
        }
    }


    public void run() {
        synchronized (this) {
            running = true;
        }

        long tm;
        while (running) {
            try {
                if ((tm = nextRunTime - currentTimeMillis()) > 0) {
                    Thread.sleep(tm);
                }
            }
            catch (InterruptedException e) {
                if (!running) return;
            }
            runAllDue();
        }

        if (autoStartStop) init();
    }

    public void start() {
        currentThread.start();
    }

    public void startIfTasks() {
        synchronized (this) {
            if (!tasks.isEmpty() && !running) currentThread.start();
        }
    }

    public void stopIfNoTasks() {
        synchronized (this) {
            if (running && tasks.isEmpty()) {
                requestStop();
            }
        }
    }

    private void runAllDue() {
        long n = 0;

        synchronized (this) {
            Iterator<TimedTask> iter = tasks.iterator();
            List<TimedTask> toRemove = new LinkedList<TimedTask>();
            TimedTask task;
            while (iter.hasNext()) {
                if ((task = iter.next()).runIfDue(n = currentTimeMillis())) {
                    if (task.nextRuntime() == -1) {
                        // if the next runtime is -1, that means this event
                        // is never scheduled to run again, so we remove it.
                        toRemove.add(task);
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

            for (TimedTask t : toRemove)
                tasks.remove(t);

            if (autoStartStop) stopIfNoTasks();
        }

        if (n == 0) nextRunTime = currentTimeMillis() + 10000;
    }

    /**
     * Adds a task to be executed.  Note: In order to remove a task, you must maintain a
     * reference to the <tt>TimedTask</tt> and set it's nextRuntime value to <tt>-1</tt>.
     * This will cause the scheduler to automatically remove it.
     *
     * @param task
     */
    public void addTask(TimedTask task) {
        synchronized (this) {
            tasks.add(task);
            if (nextRunTime == 0 || task.nextRuntime() < nextRunTime) {
                nextRunTime = task.nextRuntime();
                currentThread.interrupt();
            }

            if (autoStartStop) startIfTasks();
        }
    }


    public void setAutoStartStop(boolean autoStartStop) {
        this.autoStartStop = autoStartStop;
    }

    public void requestStop() {
        synchronized (this) {
            currentThread.interrupt();
            running = false;
        }
    }
}
