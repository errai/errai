package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.RoutingFlags;
import org.jboss.errai.bus.server.service.ErraiService;
import org.slf4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static org.jboss.errai.bus.server.util.ErrorHelper.handleMessageDeliveryFailure;
import static org.jboss.errai.bus.server.util.ErrorHelper.sendClientError;
import static org.slf4j.LoggerFactory.getLogger;

public class Worker extends Thread {
    private MessageBus bus;
    private ArrayBlockingQueue<Message> messages;
    private long timeout;

    private boolean active = true;

    private long workExpiry;
    private Message message;

    private Logger log = getLogger(this.getClass());

    public Worker(ThreadGroup threadGroup, WorkerFactory factory, ErraiService svc) {
        super(threadGroup, "Dispatch Worker Thread");
        this.timeout = factory.getWorkerTimeout();
        this.messages = factory.getMessages();
        this.bus = svc.getBus();
        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true);
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean isValid() {
        return workExpiry == 0 || currentTimeMillis() < workExpiry;
    }

    public void timeoutInterrupt() {
        interrupt();

        if (!isInterrupted() && workExpiry != 0) {
            log.info("failed to interrupt worker.");
        } else {
            workExpiry = 0;
            sendClientError(bus, message,
                    "Request for '" + message.getSubject() + "' timed out.",
                    "The process was terminated because it exceed the maximum timeout.");
        }
    }

    @Override
    public void run() {
        while (true) {
            try {
                // looping inside a catch block is cheaper than entering and leaving it
                // every time.
                while (true) {
                    if ((message = messages.poll(60, TimeUnit.SECONDS)) != null) {
                        workExpiry = currentTimeMillis() + timeout;
                        if (message.isFlagSet(RoutingFlags.NonGlobalRouting)) {
                            bus.send(message);
                        } else {
                            bus.sendGlobal(message);
                        }
                        workExpiry = 0;
                    }
                    if (!active) {
                        return;
                    }
                }
            }
            catch (InterruptedException e) {
                if (!active) return;
            }
            catch (QueueUnavailableException e) {
                handleMessageDeliveryFailure(bus, message, "Queue is not available", e, true);
            }
            catch (Throwable e) {
                handleMessageDeliveryFailure(bus, message, "Error calling remote service: " + message.getSubject(), e, false);
            }
            finally {
                workExpiry = 0;
            }
        }
    }
}
