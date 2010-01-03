package org.jboss.errai.bus.server;

import static java.lang.System.currentTimeMillis;

public abstract class TimedTask implements Runnable, Comparable<TimedTask> {
    protected long nextRuntime;
    protected long period;

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long nextRuntime() {
        return nextRuntime;
    }

    public void disable() {
        nextRuntime = -1;
    }

    public boolean runIfDue(long time) {
        if (nextRuntime < time) {
            if (nextRuntime == -1) {
            //    System.out.println("Do Not Fire:" + this);
                return false;
            }
            run();
            if (period != -1) {
                nextRuntime = currentTimeMillis() + period;
            }
            else {
                nextRuntime = -1;
            }
            return true;
        }
        return false;
    }

    public int compareTo(TimedTask o) {
        if (nextRuntime > o.nextRuntime)
            return 1;
        else if (nextRuntime < o.nextRuntime)
            return 1;
        else
            return 0;
    }
}
