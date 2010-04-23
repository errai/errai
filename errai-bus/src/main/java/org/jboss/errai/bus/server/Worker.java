package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.server.service.ErraiService;
import org.slf4j.Logger;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

import static java.lang.System.currentTimeMillis;
import static org.jboss.errai.bus.server.util.ErrorHelper.handleMessageDeliveryFailure;
import static org.jboss.errai.bus.server.util.ErrorHelper.sendClientError;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * A <tt>Worker</tt> is a specialized thread made to work with the messages and services of Errai
 */
public class Worker extends Thread {
    private MessageBus bus;
    private ArrayBlockingQueue<Message> messages;
    private long timeout;

    private boolean active = true;

    private volatile long workExpiry;
    private Message message;

    private Logger log = getLogger(this.getClass());

    /**
     * Initializes the thread with the specified <tt>ThreadGroup</tt>, <tt>factory</tt> and service
     *
     * @param factory - the factory this worker thread will belong to
     * @param svc - the service the thread is attached to
     */
    public Worker(WorkerFactory factory, ErraiService svc) {
        super("Dispatch Worker Thread");
        this.timeout = factory.getWorkerTimeout();
        this.messages = factory.getMessages();
        this.bus = svc.getBus();
        setPriority(Thread.MIN_PRIORITY);
        setDaemon(true);
    }

    /**
     * Sets the <tt>Worker</tt> to an active or inactive state
     *
     * @param active - true if the thread should be active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns true if this thread is valid, and hasn't expired
     *
     * @return false if this thread is invalid or has expired
     */
    public boolean isValid() {
        return workExpiry == 0 || currentTimeMillis() < workExpiry;
    }

    /**
     * Interrupts this worker thread, and expire it due to a timeout.
     * Creates an error message if they could not be interrupted  
     */
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

    /**
     * Runs the thread, setting the expiry time, and sends the messages associated with this thread 
     */
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
            catch (QueueOverloadedException e) {
                handleMessageDeliveryFailure(bus, message, "Queue has become saturated/overloaded", e, true);
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
