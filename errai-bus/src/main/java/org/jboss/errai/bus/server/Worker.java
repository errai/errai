package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.service.ErraiService;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker extends Thread {
    private ArrayBlockingQueue<CommandMessage> messages;
    private MessageBus bus;
    private boolean active = true;

    public Worker(ArrayBlockingQueue<CommandMessage> messageQueue, ErraiService svc) {
        this.messages = messageQueue;
        this.bus = svc.getBus();
        setPriority(Thread.MIN_PRIORITY);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void run() {
        while (true) {
            CommandMessage m;
            try {
                if ((m = messages.poll(45, TimeUnit.SECONDS)) == null) {
                    continue;
                } else {
                    bus.sendGlobal(m);
                }
            }
            catch (InterruptedException e) {
                if (!active) return;
            }
        }
    }
}
