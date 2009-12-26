package org.jboss.errai.bus.server;

public abstract class TimedTask implements Runnable {
    private long lastRunTime;
    protected long period;

    public long getPeriod() {
        return period;
    }

    public void setPeriod(long period) {
        this.period = period;
    }

    public void runIfDue(long time) {
        if ((lastRunTime + period) < time) {
            lastRunTime = System.currentTimeMillis();
            run();
        }
    }
}
