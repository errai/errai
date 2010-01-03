package org.jboss.errai.bus.server;

public abstract class TimedTask implements Runnable {
    protected long nextRunTime;
    protected long period;

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public long nextRuntime() {
        return nextRunTime;
    }

    public void disable() {
        nextRunTime = -1;
    }

    public long runIfDue(long time) {
        if (nextRunTime == -1) return -1;
        else if ((nextRunTime) < time) {
            run();
            if (period != -1) {
                nextRunTime = System.currentTimeMillis() + period;
            }
        }
        return nextRunTime;
    }
}
