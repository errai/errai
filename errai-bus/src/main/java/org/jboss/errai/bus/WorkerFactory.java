package org.jboss.errai.bus;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.Worker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class WorkerFactory {
    private int size;
    private Worker[] workerPool;
    private MessageBus bus;
    private ArrayBlockingQueue<CommandMessage> messages;

    private int idx = 0;

    public WorkerFactory(int size, MessageBus bus) {
        this.messages = new ArrayBlockingQueue<CommandMessage>(100);
        this.workerPool = new Worker[this.size = size];
        this.bus = bus;

        for (int i = 0; i < size; i++) {
            workerPool[i] = new Worker(messages, bus);
        }
    }

    public void deliver(CommandMessage m) {
        messages.offer(m);
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
