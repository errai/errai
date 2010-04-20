/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.framework.Payload;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

/**
 * A message queue is keeps track of which messages need to be sent outbound. It keeps track of the amount of messages
 * that can be stored, transmitted and those which timeout. The <tt>MessageQueue</tt> is implemented using a
 * {@link java.util.concurrent.LinkedBlockingQueue} to store the messages, and a <tt>ServerMessageBus</tt> to send the
 * messages.
 */
public class MessageQueue {
    private static final long TIMEOUT = Boolean.getBoolean("org.jboss.errai.debugmode") ?
            secs(60) : secs(25);

    private static final int MAXIMUM_PAYLOAD_SIZE = 10;
    private static final long DEFAULT_TRANSMISSION_WINDOW = millis(25);
    private static final long MAX_TRANSMISSION_WINDOW = millis(100);

    private QueueSession session;

    private long transmissionWindow = 50;
    private long lastTransmission = System.nanoTime();
    private long endWindow;

    private int lastQueueSize = 0;
    private boolean throttleIncoming = false;
    private boolean queueRunning = true;

    private boolean _windowPolling = false;
    private boolean windowPolling = false;

    private SessionControl sessionControl;
    private QueueActivationCallback activationCallback;
    private BlockingQueue<MarshalledMessage> queue;

    private ServerMessageBus bus;
    private volatile TimedTask task;

    private final Semaphore lock = new Semaphore(1, true);

    /**
     * Initializes the message queue with an initial size and a specified bus
     *
     * @param queueSize - the size of the queue
     * @param bus       - the bus that will send the messages
     * @param session   - the session associated with the queue
     */
    public MessageQueue(int queueSize, ServerMessageBus bus, QueueSession session) {
        this.queue = new LinkedBlockingQueue<MarshalledMessage>(queueSize);
        this.bus = bus;
        this.session = session;
    }

    /**
     * Gets the next message to send, and returns the <tt>Payload</tt>, which contains the current messages that
     * need to be sent from the specified bus to another.
     *
     * @param wait - boolean is true if we should wait until the queue is ready. In this case, a
     *             <tt>RuntimeException</tt> will be thrown if the polling is active already. Concurrent polling is not allowed.
     * @return The <tt>Payload</tt> instance which contains the messages that need to be sent
     */
    public Payload poll(boolean wait) {
        if (!queueRunning) {
            throw new QueueUnavailableException("queue is not available");
        }

        checkSession();

        if (lock.tryAcquire()) {
            try {
                MarshalledMessage m;

                if (wait) {
                    m = queue.poll(45, TimeUnit.SECONDS);

                } else {
                    m = queue.poll();
                }


                int payLoadSize = 0;

                Payload p = new Payload(m == null ? heartBeat : m);

                if (_windowPolling) {
                    windowPolling = true;
                    _windowPolling = false;
                } else if (windowPolling) {
                    while (!queue.isEmpty() && payLoadSize < MAXIMUM_PAYLOAD_SIZE
                            && !isWindowExceeded()) {
                        p.addMessage(queue.poll());
                        payLoadSize++;

                        try {
                            if (queue.isEmpty())
                                Thread.sleep(System.nanoTime() - endWindow);
                        }
                        catch (Exception e) {
                            // just resume.
                        }
                    }

                    if (!throttleIncoming && queue.size() > lastQueueSize) {
                        if (transmissionWindow < MAX_TRANSMISSION_WINDOW) {
                            transmissionWindow += millis(50);
                            System.err.println("[Congestion on queue -- New transmission window: "
                                    + transmissionWindow + "; Queue size: " + queue.size() + ")]");
                        } else {
                            throttleIncoming = true;
                            System.err.println("[Warning: A queue has become saturated and " +
                                    "performance is now being degraded.]");
                        }

                    } else if (queue.isEmpty()) {
                        transmissionWindow = DEFAULT_TRANSMISSION_WINDOW;
                        throttleIncoming = false;
                    }
                }

                lastQueueSize = queue.size();
                endWindow = (lastTransmission = System.nanoTime()) + transmissionWindow;

                return p;

            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                lock.release();
            }

        }
        return new Payload(heartBeat);
    }

    /**
     * Inserts the specified message into the queue, and returns true if it was successful
     *
     * @param message - the message to insert into the queue
     * @return true if insertion was successful
     */
    public boolean offer(final MarshalledMessage message) {
        if (!queueRunning) {
            throw new QueueUnavailableException("queue is not available");
        }

        boolean b = false;
        activity();
        try {
            b = (throttleIncoming ? queue.offer(message, 250, TimeUnit.MILLISECONDS) : queue.offer(message));
        }
        catch (InterruptedException e) {
            // fall-through.
        }

        if (!b) {
            queue.clear();
            throw new QueueOverloadedException("too many undelievered messages in queue: cannot dispatch message.");
        } else if (activationCallback != null) {
            if (isWindowExceeded()) {
                descheduleTask();
                activationCallback.activate(this);
            } else if (task == null) {
                scheduleActivation();
            }
        }

        return b;
    }

    /**
     * Schedules the activation, by sending off the queue. All message should be processed and sent once the task is
     * processed
     */
    public void scheduleActivation() {
        bus.getScheduler().addTask(
                task = new TimedTask() {
                    {
                        period = -1; // only fire once.
                        nextRuntime = getEndOfWindow();
                    }

                    public void run() {

                        if (activationCallback != null)
                            activationCallback.activate(MessageQueue.this);

                        task = null;
                    }

                    @Override
                    public String toString() {
                        return "MessageResumer";
                    }
                }
        );
    }

    private void checkSession() {
        if (sessionControl != null && !sessionControl.isSessionValid()) {
            System.out.println("SessionExpired");
            throw new MessageQueueExpired("session has expired");
        }
    }

    /**
     * This function indicates activity on the session, so the session knows when the last time there was activity.
     * The <tt>MessageQueue</tt> relies on this to figure out whether or not to timeout
     */
    public void activity() {
        if (sessionControl != null) sessionControl.activity();
    }

    private boolean isWindowExceeded() {
        return System.nanoTime() > endWindow;
    }

    private long getEndOfWindow() {
        return endWindow - System.nanoTime();
    }

    private void descheduleTask() {
        if (task != null) {
            task.disable();
            task = null;
        }
    }

    /**
     * Returns true if there is a message in the queue
     *
     * @return true if the queue is not empty
     */
    public boolean messagesWaiting() {
        return !queue.isEmpty();
    }

    /**
     * Sets the activation callback function which is called when the queue is scheduled for activation
     *
     * @param activationCallback - new activation callback function
     */
    public void setActivationCallback(QueueActivationCallback activationCallback) {
        this.activationCallback = activationCallback;
    }

    /**
     * Returns the current activation callback function
     *
     * @return the current activation callback function
     */
    public QueueActivationCallback getActivationCallback() {
        return activationCallback;
    }

    /**
     * Returns the current queue that is storing the messages
     *
     * @return the queue containing the messages to be sent
     */
    public BlockingQueue<MarshalledMessage> getQueue() {
        return queue;
    }

    public QueueSession getSession() {
        return session;
    }

    /**
     * Returns true if the queue is not running, or it has timed out
     *
     * @return true if the queue is stale
     */
    public boolean isStale() {
        return !queueRunning || (!isActive() && (System.nanoTime() - lastTransmission) > TIMEOUT);
    }

    /**
     * Returns true if the queue is currently active and polling
     *
     * @return true if the queue is actively polling
     */
    public boolean isActive() {
        return lock.availablePermits() == 0;
    }

    /**
     * Fakes a transmission, shows life with a heartbeat
     */
    public void heartBeat() {
        lastTransmission = System.nanoTime();
    }

    /**
     * Returns true if the window is polling
     *
     * @return true if the window is polling
     */
    public boolean isWindowPolling() {
        return windowPolling;
    }

    /**
     * Sets window polling
     *
     * @param windowPolling -
     */
    public void setWindowPolling(boolean windowPolling) {
        this._windowPolling = windowPolling;
    }


    /**
     * Stops the queue, closes it on the bus and clears it completely
     */
    public void stopQueue() {
        queueRunning = false;
        queue.clear();
        bus.closeQueue(this);
    }

    private static final MarshalledMessage heartBeat = new MarshalledMessage() {
        public String getSubject() {
            return "HeartBeat";
        }

        public Object getMessage() {
            return null;
        }
    };

    private static long secs(long secs) {
        return secs * 1000000000;
    }

    private static long millis(long millis) {
        return millis * 1000000;
    }
}
