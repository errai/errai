package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;

import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class Worker extends Thread {
    private ArrayBlockingQueue<CommandMessage> messages = new ArrayBlockingQueue<CommandMessage>(25);
    private MessageBus bus;
    private boolean active = true;

    public Worker(MessageBus bus) {
        this.bus = bus;
        setPriority(Thread.MIN_PRIORITY);
    }

    public void deliver(CommandMessage m) {
        messages.offer(m);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public void run() {
        while (true) {
            try {
                CommandMessage m = messages.poll(45, TimeUnit.SECONDS);
                if (m == null) {
                    continue;
                } else if (m.getResource("sendGlobal") != null) {
                    bus.sendGlobal(m);
                } else {
                    bus.send(m);
                }
            }
            catch (InterruptedException e) {
                if (!active) return;
            }
        }
    }
}
