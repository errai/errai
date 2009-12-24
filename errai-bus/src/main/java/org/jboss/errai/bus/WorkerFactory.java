package org.jboss.errai.bus;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.Worker;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class WorkerFactory {
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    private static final String CONFIG_ASYNC_THREAD_POOL_SIZE = "errai.async.thread_pool_size";

    private int size;
    private Worker[] workerPool;
    private ErraiService svc;
    private ArrayBlockingQueue<CommandMessage> messages;

    private int idx = 0;

    public WorkerFactory(ErraiService svc) {
        this.messages = new ArrayBlockingQueue<CommandMessage>(100);
        ErraiServiceConfigurator cfg = svc.getConfiguration();

        if (cfg.hasProperty(CONFIG_ASYNC_THREAD_POOL_SIZE)) {
            size = Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_THREAD_POOL_SIZE));
        }
        else {
            size = DEFAULT_THREAD_POOL_SIZE;
        }

        this.workerPool = new Worker[size];
        this.svc = svc;

        for (int i = 0; i < size; i++) {
            workerPool[i] = new Worker(messages, svc);
        }

        startPool();
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
