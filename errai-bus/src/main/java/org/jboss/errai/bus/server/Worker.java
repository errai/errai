package org.jboss.errai.bus.server;

import org.jboss.errai.bus.server.WorkerFactory;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.service.ErraiService;

import java.util.concurrent.TimeUnit;

public class Worker extends Thread {
    private WorkerFactory workerFactory;
    private MessageBus bus;
    private boolean active = true;
    private long workExpiry;

    public Worker(WorkerFactory factory, ErraiService svc) {
        this.workerFactory = factory;
        this.bus = svc.getBus();
        setPriority(Thread.MIN_PRIORITY);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValid() {
        return workExpiry == 0 || System.currentTimeMillis() < workExpiry;
    }

    @Override
    public void run() {
        while (true) {
            CommandMessage m;
            try {
                if ((m = workerFactory.getMessages().poll(workerFactory.getWorkerTimeout(), TimeUnit.SECONDS)) == null) {
                    continue;
                } else {
                    workExpiry = System.currentTimeMillis() + (workerFactory.getWorkerTimeout() * 1000);
                    bus.sendGlobal(m);
                    workExpiry = 0;
                }
            }
            catch (InterruptedException e) {
                if (!active) return;
                e.printStackTrace();
            }
        }
    }
}
