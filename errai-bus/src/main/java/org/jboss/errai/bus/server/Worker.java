package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.RoutingFlags;
import org.jboss.errai.bus.server.service.ErraiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class Worker extends Thread {
    private WorkerFactory workerFactory;
    private MessageBus bus;

    private boolean active = true;
    private boolean timeout = false;

    private long workExpiry;
    private CommandMessage message;

    private Logger log = LoggerFactory.getLogger(this.getClass());

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

    public void timeoutInterrupt() {
        timeout = true;
        interrupt();

        if (!isInterrupted()) {
            log.info("failed to interrupt worker.");
        } else {
            workExpiry = 0;
            timeout = false;
            ConversationMessage.create(message)
                    .toSubject("ClientBusErrors")
                    .set("ErrorMessage", "Request for '" + message.getSubject() + "' timed out.")
                    .set("AdditionalDetails", "The process was terminated because it exceeded the maximum timeout.")
                    .sendNowWith(bus);
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
                    workExpiry = 0;
                }
            }
            catch (InterruptedException e) {
                if (!active) return;
            }
        }
    }
}
