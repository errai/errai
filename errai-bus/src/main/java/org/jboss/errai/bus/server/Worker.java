package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.*;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.util.ErrorHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static org.jboss.errai.bus.server.util.ErrorHelper.sendClientError;

public class Worker extends Thread {
    private WorkerFactory workerFactory;
    private MessageBus bus;

    private boolean active = true;

    private long workExpiry;
    private Message message;

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public Worker(WorkerFactory factory, ErraiService svc) {
        this.workerFactory = factory;
        this.bus = svc.getBus();
        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValid() {
        return workExpiry == 0 || System.currentTimeMillis() < workExpiry;
    }

    public void timeoutInterrupt() {
      //  timeout = true;
        interrupt();

        if (!isInterrupted()) {
            log.info("failed to interrupt worker.");
        } else {
            workExpiry = 0;
       //     timeout = false;

            sendClientError(bus, message,
                    "Request for '" + message.getSubject() + "' timed out.",
                    "The process was terminated because it exceed the maximum timeout.");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                if ((message = workerFactory.getMessages().poll(workerFactory.getWorkerTimeout(), TimeUnit.MILLISECONDS)) == null) {
                    continue;
                } else {
                    workExpiry = System.currentTimeMillis() + workerFactory.getWorkerTimeout();
                    if (message.isFlagSet(RoutingFlags.NonGlobalRouting)) {
                        bus.send(message);
                    } else {
                        bus.sendGlobal(message);
                    }
                }
            }
            catch (InterruptedException e) {
                if (!active) return;
            }
            catch (Throwable e) {
                if (message.getErrorCallback() != null) {
                    if (!message.getErrorCallback().error(message, e)) {
                        continue;
                    }
                }
                ErrorHelper.sendClientError(bus, message,
                        "Error calling remote service: " + message.getSubject(), e);
            }
            finally {
                workExpiry = 0;
            }
        }
    }
}
