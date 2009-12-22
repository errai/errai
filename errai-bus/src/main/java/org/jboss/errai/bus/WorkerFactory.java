package org.jboss.errai.bus;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.Worker;

import java.util.ArrayList;
import java.util.List;

public class WorkerFactory {
    private int size;
    private Worker[] workerPool;
    private MessageBus bus;

    private int idx = 0;

    public WorkerFactory(int size, MessageBus bus) {
        this.workerPool = new Worker[this.size = size];
        this.bus = bus;

        for (int i = 0; i < size; i++) {
            workerPool[i] = new Worker(bus);
        }
    }

    public void deliver(CommandMessage m) {
        if (idx == size) idx = 0;
        workerPool[idx++].deliver(m);
    }

    public void startPool() {
        for (int i = 0; i < size; i++) {
            workerPool[i].start();
        }
    }

    public enum Strategy {
        ROUND_ROBIN
    }
}
