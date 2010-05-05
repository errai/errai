package org.jboss.errai.bus.server.async;

import org.jboss.errai.bus.client.api.AsyncTask;

import static java.lang.System.currentTimeMillis;

/**
 * A <tt>TimedTask</tt> is used for scheduling tasks, and making sure they are run at appropriate times and intervals
 */
public abstract class TimedTask implements Runnable, Comparable<TimedTask>, AsyncTask {

    protected volatile long lastRuntime;
    protected volatile long nextRuntime;
    protected volatile long period;
    protected volatile boolean cancel = false;


    /**
     * Gets the period of the task, and when it should be run next
     *
     * @return the interval length
     */
    public long getPeriod() {
        return period;
    }

    /**
     * Sets the period in which the task should be run next
     *
     * @param period
     */
    public void setPeriod(long period) {
        this.period = period;
    }


    public void cancelAfterNextFire() {
        period = -1;
    }

    /**
     * Gets the time in which the task will be run.  If -1 is returned, the task is permanently de-scheduled.
     *
     * @return the time the task will be run in milliseconds
     */
    public long nextRuntime() {
        return nextRuntime;
    }

    public boolean isStillActive() {
        return nextRuntime == -1;
    }

    /**
     * Disables the task
     */
    public void disable() {
        cancel = true;
    }

    public boolean cancel(boolean interrupt) {
        disable();
        return true;
    }

    /**
     * Returns true if the task has been cancelled or is expired.
     *
     * @return
     */
    public boolean isCancelled() {
        return cancel;
    }

    /**
     * Runs the task if the specified time is greater than the tasks runtime, and the task is still enabled. If the
     * next runtime for the task shall happen before <tt>time</tt>, nothing is done or changed
     *
     * @param time - the time in which the task should be run
     * @return true if the task was run
     */
    public boolean runIfDue(long time) {
        synchronized (this) {
            if (isDue(time)) {
                if (nextRuntime == -1) {
                    return false;
                }
                calculateNextRuntime();
                run();
                return true;
            }
            return false;
        }
    }

    public void runNow() {
        synchronized (this) {
            if (nextRuntime == -1) {
                return;
            }
            run();
            lastRuntime = System.currentTimeMillis();
        }
    }

    public void calculateNextRuntime() {
        synchronized (this) {
            if (period != -1) {
                nextRuntime = currentTimeMillis() + period;
            } else {
                nextRuntime = -1;
            }
        }
    }


    public boolean isDue(long time) {
        synchronized (this) {
            return !cancel && nextRuntime < time && nextRuntime != -1;
        }
    }

    public long getLastRuntime() {
        return lastRuntime;
    }

    public int compareTo(TimedTask o) {
        if (o == this) {
            return 0;
        }
        if (nextRuntime > o.nextRuntime)
            return 1;
        else if (nextRuntime < o.nextRuntime)
            return -1;
        else
            return 0;
    }
}
