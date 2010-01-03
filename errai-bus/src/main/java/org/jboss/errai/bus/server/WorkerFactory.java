package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.RoutingFlags;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.util.ErrorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ArrayBlockingQueue;

public class WorkerFactory {
    private static final int DEFAULT_DELIVERY_QUEUE_SIZE = 250;
    private static final int DEFAULT_THREAD_POOL_SIZE = 4;

    private static final String CONFIG_ASYNC_THREAD_POOL_SIZE = "errai.async.thread_pool_size";
    private static final String CONFIG_ASYNC_WORKER_TIMEOUT = "errai.async.worker.timeout";
    private static final String CONFIG_ASYNC_DELIVERY_QUEUE_SIZE = "errai.async.delivery.queue_size";
    private Worker[] workerPool;

    private ErraiService svc;

    private ArrayBlockingQueue<Message> messages;

    private int poolSize = DEFAULT_THREAD_POOL_SIZE;
    private int deliveryQueueSize = DEFAULT_DELIVERY_QUEUE_SIZE;
    private long workerTimeout = seconds(30);

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public WorkerFactory(ErraiService svc) {
        this.svc = svc;

        ErraiServiceConfigurator cfg = svc.getConfiguration();

        if (cfg.hasProperty(CONFIG_ASYNC_DELIVERY_QUEUE_SIZE)) {
            deliveryQueueSize = Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_DELIVERY_QUEUE_SIZE));
        }

        if (cfg.hasProperty(CONFIG_ASYNC_THREAD_POOL_SIZE)) {
            poolSize = Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_THREAD_POOL_SIZE));
        }

        if (cfg.hasProperty(CONFIG_ASYNC_WORKER_TIMEOUT)) {
            workerTimeout = seconds(Integer.parseInt(cfg.getProperty(CONFIG_ASYNC_WORKER_TIMEOUT)));
        }

        this.messages = new ArrayBlockingQueue<Message>(deliveryQueueSize);

        log.info("initializing async worker pools (poolSize: " + poolSize + "; workerTimeout: " + workerTimeout + ")");

        this.workerPool = new Worker[poolSize];

        for (int i = 0; i < poolSize; i++) {
            workerPool[i] = new Worker(this, svc);
        }

        if (svc.getBus() instanceof ServerMessageBusImpl) {
            ServerMessageBusImpl busImpl = (ServerMessageBusImpl) svc.getBus();

            System.out.println("AddTask");
            /**
             * Add a housekeeper task to the bus housekeeper to timeout long-running tasks.
             */
            busImpl.getScheduler().addTask(new TimedTask() {
                {
                    period = 1000;
                }
                public void run() {
                    for (Worker w : workerPool) {
                        if (!w.isValid()) {
                            log.warn("Terminating worker.  Process exceeds maximum time to live.");
                            w.timeoutInterrupt();
                        }
                    }
                }

                @Override
                public String toString() {
                    return "WorkerTimeout";
                }
            });
            System.out.println("Added Task");
        }

        System.out.println("startPool()");
        startPool();
    }

    public void deliverGlobal(Message m) {
        if (messages.offer(m)) {
            return;
        } else {
            sendDeliveryFailure(m);
            throw new RuntimeException("delivery queue is overloaded!");
        }
    }

    public void deliver(Message m) {
        m.setFlag(RoutingFlags.NonGlobalRouting);
        if (messages.offer(m)) {
            return;
        } else {
            sendDeliveryFailure(m);
            throw new RuntimeException("delivery queue is overloaded!");
        }
    }

    private void sendDeliveryFailure(Message m) {
        MessageDeliveryFailure mdf
                = new MessageDeliveryFailure("could not deliver message because the outgoing queue is full");

        if (m.getErrorCallback() == null || m.getErrorCallback().error(m, mdf)) {
            ErrorHelper.sendClientError(svc.getBus(), m, mdf.getMessage(), mdf);
            throw mdf;
        }
    }

    protected ArrayBlockingQueue<Message> getMessages() {
        return messages;
    }

    protected long getWorkerTimeout() {
        return workerTimeout;
    }

    public void startPool() {
        log.info("starting worker pool.");
        for (int i = 0; i < poolSize; i++) {
            workerPool[i].start();
        }
    }

    private long seconds(int seconds) {
        return seconds * 1000;
    }
}
