package org.jboss.errai.bus.server;

import java.util.*;

public class Scheduler extends Thread {
    private boolean running = true;
    private long nextRunTime = 0;
    private ServerMessageBusImpl bus;
    private Set<TimedTask> tasks = new LinkedHashSet<TimedTask>();

    public Scheduler(final ServerMessageBusImpl bus) {
        super();
        this.bus = bus;
        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void run() {

        while (running) {
            try {
                long sleep = nextRunTime = System.currentTimeMillis();
                if (sleep > 0) {
                    Thread.sleep(sleep);
                }
            }
            catch (InterruptedException e) {
                if (!running) return;
            }
            runAllDue();
        }

    }

    private void runAllDue() {
        long nextRun;
        Iterator<TimedTask> iter = tasks.iterator();
        while (iter.hasNext()) {
            nextRun = iter.next().runIfDue(System.currentTimeMillis());
            if (nextRun == -1) {
                iter.remove();
            } else if (nextRun < nextRunTime) {
                nextRunTime = nextRun;
            }
        }

    }

    public void addTask(TimedTask task) {
        if (task.nextRuntime() < nextRunTime) {
            nextRunTime = nextRunTime - System.currentTimeMillis();
            interrupt();
        }
        tasks.add(task);
    }

    public void removeTask(TimedTask task) {
        tasks.remove(task);
    }
}
