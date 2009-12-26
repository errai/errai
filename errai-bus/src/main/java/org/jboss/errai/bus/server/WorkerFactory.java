package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

public class WorkerFactory {
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    private static final String CONFIG_ASYNC_THREAD_POOL_SIZE = "errai.async.thread_pool_size";
    private static final String CONFIG_ASYNC_WORKER_TIMEOUT = "errai.async.worker.timeout";
    private Worker[] workerPool;

    private ErraiService svc;
    private ArrayBlockingQueue<CommandMessage> messages;

    private int poolSize = DEFAULT_THREAD_POOL_SIZE;
    private long workerTimeout = seconds(30);

    private int idx = 0;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public WorkerFactory(ErraiService svc) {
        this.messages = new ArrayBlockingQueue<CommandMessage>(100);
        ErraiServiceConfigurator cfg = svc.getConfiguration();

        if (cfg.hasProperty(CONFIG_ASYNC_THREAD_POOL_SIZE)) {
            poolSize = Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_THREAD_POOL_SIZE));
        }

        if (cfg.hasProperty(CONFIG_ASYNC_WORKER_TIMEOUT)) {
            workerTimeout = seconds(Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_WORKER_TIMEOUT)));
        }


        this.workerPool = new Worker[poolSize];
        this.svc = svc;

        for (int i = 0; i < poolSize; i++) {
            workerPool[i] = new Worker(this, svc);
        }

        if (svc.getBus() instanceof ServerMessageBusImpl) {
            ServerMessageBusImpl busImpl = (ServerMessageBusImpl) svc.getBus();

            /**
             * Add a housekeeper task to the bus housekeeper to timeout long-running tasks.
             */
            busImpl.getHouseKeeper().addTask(new TimedTask() {
                {
                    period = 1000;
                }

                public void run() {
                    for (Worker w : workerPool) {
                        if (!w.isValid()) {
                            log.warn("Terminating worker.  Process exceeds maximum time to live.");
                            w.interrupt();
                        }
                    }
                }
            });
        }

        startPool();
    }

    public void deliver(CommandMessage m) {
        messages.offer(m);
    }

    protected ArrayBlockingQueue<CommandMessage> getMessages() {
        return messages;
    }

    protected long getWorkerTimeout() {
        return workerTimeout;
    }

    public void startPool() {
        for (int i = 0; i < poolSize; i++) {
            workerPool[i].start();
        }
    }

    private long seconds(int seconds) {
        return seconds * 1000;
    }

    public enum Strategy {
        ROUND_ROBIN
    }
}
