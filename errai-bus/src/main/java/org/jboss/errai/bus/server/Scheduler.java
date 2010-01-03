package org.jboss.errai.bus.server;

import java.util.*;

import static java.lang.System.currentTimeMillis;

public class Scheduler extends Thread {
    private boolean running = true;
    private long nextRunTime = 0;
    // private ServerMessageBusImpl bus;
    private final TreeSet<TimedTask> tasks = new TreeSet<TimedTask>();

    public Scheduler() {
        super();
        //   this.bus = bus;
        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true);
    }

    @Override
    public void run() {
        while (running) {
            try {
                long sleep = nextRunTime - currentTimeMillis();
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
        long n = 0;

        synchronized (tasks) {
            Iterator<TimedTask> iter = tasks.iterator();
            TimedTask task;
            while (iter.hasNext()) {
                if ((task = iter.next()).runIfDue(n = currentTimeMillis())) {
                    if (task.nextRuntime() == -1) {
                        iter.remove();
                    } else {
                        nextRunTime = task.nextRuntime();
                    }
                } else if (task.nextRuntime() == -1) {
                    iter.remove();
                } else if (nextRunTime == 0 || task.nextRuntime() < nextRunTime) {
                    nextRunTime = task.nextRuntime();
                } else if (n > task.nextRuntime()) {
                    return;
                }
            }
        }

        if (n == 0) nextRunTime = currentTimeMillis() + 10000;
    }

    public void addTask(TimedTask task) {
   //     System.out.println("Task Scheduled: " + task);
        synchronized (tasks) {
            tasks.add(task);
            if (nextRunTime == 0 || task.nextRuntime() < nextRunTime) {
                nextRunTime = task.nextRuntime();
                interrupt();
            }
        }
    }

    public void removeTask(TimedTask task) {
        synchronized (tasks) {
            tasks.remove(task);
        }
    }
}
