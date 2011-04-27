/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

import org.jboss.errai.bus.client.api.HasEncoded;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.QueueStopMessage;
import org.jboss.errai.bus.server.api.*;
import org.jboss.errai.bus.server.async.TimedTask;
import org.jboss.errai.bus.server.io.JSONStreamEncoder;
import org.mvel2.util.StringAppender;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.lang.System.nanoTime;

/**
 * A message queue is keeps track of which messages need to be sent outbound. It keeps track of the amount of messages
 * that can be stored, transmitted and those which timeout. The <tt>MessageQueue</tt> is implemented using a
 * {@link java.util.concurrent.LinkedBlockingQueue} to store the messages, and a <tt>ServerMessageBus</tt> to send the
 * messages.
 */
public class MessageQueueImpl implements MessageQueue {
    private static final long TIMEOUT = Boolean.getBoolean("org.jboss.errai.debugmode") ?
            secs(60) : secs(30);

    private static final int MAXIMUM_PAYLOAD_SIZE = 10;
    private static final long DEFAULT_TRANSMISSION_WINDOW = millis(25);
    private static final long MAX_TRANSMISSION_WINDOW = millis(100);

    private final QueueSession session;

    private long transmissionWindow = 40;
    private volatile long lastTransmission = nanoTime();
    private long endWindow;

    private int lastQueueSize = 0;
    private boolean throttleIncoming = false;
    private boolean queueRunning = true;

    private boolean _windowPolling = false;
    private boolean windowPolling = false;

    private SessionControl sessionControl;
    private QueueActivationCallback activationCallback;
    private BlockingQueue<Message> queue;


    private final ServerMessageBus bus;
    private volatile TimedTask task;

    private final Semaphore lock = new Semaphore(1, true);
    private volatile boolean initLock = true;
    private final Object activationLock = new Object();


    public static class OutputStreamCapture extends OutputStream {
        private OutputStream wrap;
        private StringAppender buf = new StringAppender();


        public OutputStreamCapture(OutputStream wrap) {
            this.wrap = wrap;
        }

        @Override
        public void write(int b) throws IOException {
            buf.append((char) b);
            wrap.write(b);
        }

        @Override
        public String toString() {
            return buf.toString();
        }
    }

    /**
     * Initializes the message queue with an initial size and a specified bus
     *
     * @param queueSize - the size of the queue
     * @param bus       - the bus that will send the messages
     * @param session   - the session associated with the queue
     */
    public MessageQueueImpl(final int queueSize, final ServerMessageBus bus, final QueueSession session) {
        this.queue = new LinkedBlockingQueue<Message>(queueSize);
        this.bus = bus;
        this.session = session;
    }

    /**
     * Gets the next message to send, and returns the <tt>Payload</tt>, which contains the current messages that
     * need to be sent from the specified bus to another.
     *
     * @param wait      - boolean is true if we should wait until the queue is ready. In this case, a
     *                  <tt>RuntimeException</tt> will be thrown if the polling is active already. Concurrent polling is not allowed.
     * @param outstream - output stream to write the polling results to.
     */
    public void poll(final boolean wait, final OutputStream outstream) throws IOException {
        if (!queueRunning) {
            JSONStreamEncoder.encode(new QueueStopMessage().getParts(), outstream);
            return;
        }

        Message m = null;

        checkSession();

        outstream.write('[');

        if (lock.tryAcquire()) {
            int payLoadSize = 0;
            try {

                if (wait) {
                    m = queue.poll(45, TimeUnit.SECONDS);

                } else {
                    m = queue.poll();
                }

                if (m instanceof HasEncoded) {
                    outstream.write(((HasEncoded) m).getEncoded().getBytes());
                } else if (m instanceof QueueStopMessage) {
                    JSONStreamEncoder.encode(m.getParts(), outstream);
                    queueRunning = false;
                    bus.closeQueue(this);
                }
                else if (m != null) {
                    JSONStreamEncoder.encode(m.getParts(), outstream);
                }

                if (_windowPolling) {
                    windowPolling = true;
                    _windowPolling = false;
                } else if (windowPolling) {
                    while (!queue.isEmpty() && payLoadSize < MAXIMUM_PAYLOAD_SIZE
                            && !isWindowExceeded()) {
                        outstream.write(',');
                        if ((m = queue.poll()) instanceof HasEncoded) {
                            outstream.write(((HasEncoded) m).getEncoded().getBytes());
                        } else {
                            JSONStreamEncoder.encode(m.getParts(), outstream);
                        }
                        payLoadSize++;

                        try {
                            if (queue.isEmpty())
                                Thread.sleep(nanoTime() - endWindow);
                        }
                        catch (Exception e) {
                            // just resume.
                        }
                    }

                    if (!throttleIncoming && queue.size() > lastQueueSize) {
                        if (transmissionWindow < MAX_TRANSMISSION_WINDOW) {
                            transmissionWindow += millis(50);
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
                endWindow = (lastTransmission = nanoTime()) + transmissionWindow;

                if (m == null) outstream.write(heartBeatBytes);

                outstream.write(']');
                return;

            }
            catch (InterruptedException e) {
                e.printStackTrace();
            }
            finally {
                lock.release();
            }

        }

        if (m == null) outstream.write(heartBeatBytes);
        outstream.write(']');
    }

    private static final byte[] heartBeatBytes = "{ToSubject:\"ClientBus\", CommandType:\"Heartbeat\"}".getBytes();

    /**
     * Inserts the specified message into the queue, and returns true if it was successful
     *
     * @param message - the message to insert into the queue
     * @return true if insertion was successful
     */
    public boolean offer(final Message message) {
        if (!queueRunning) {
            throw new QueueUnavailableException("queue is not available");
        }

        boolean b = false;
        activity();
        try {
            b = (throttleIncoming ? queue.offer(message, 1, TimeUnit.SECONDS) : queue.offer(message));
        }
        catch (InterruptedException e) {
            // fall-through.
        }

        if (!b) {
            queue.clear();
            throw new QueueOverloadedException(null, "too many undelievered messages in queue: cannot dispatch message.");
        } else if (activationCallback != null) {
            synchronized (activationLock) {
                if (isWindowExceeded()) {
                    descheduleTask();
                    if (activationCallback != null) activationCallback.activate(this);
                } else if (task == null) {
                    scheduleActivation();
                }
            }
        }

        return b;
    }

    /**
     * Schedules the activation, by sending off the queue. All message should be processed and sent once the task is
     * processed
     */
    public void scheduleActivation() {
        synchronized (activationLock) {
            bus.getScheduler().addTask(
                    task = new TimedTask() {
                        {
                            period = -1; // only fire once.
                            nextRuntime = getEndOfWindow();
                        }

                        public void run() {
                            if (activationCallback != null)
                                activationCallback.activate(MessageQueueImpl.this);

                            task = null;
                        }

                        public boolean isFinished() {
                            return false;
                        }

                        @Override
                        public String toString() {
                            return "MessageResumer";
                        }
                    }
            );
        }
    }

    private void checkSession() {
        if (sessionControl != null && !sessionControl.isSessionValid()) {
            throw new MessageQueueExpired("session has expired");
        }
    }

    public void setSessionControl(SessionControl sessionControl) {
        this.sessionControl = sessionControl;
    }

    /**
     * This function indicates activity on the session, so the session knows when the last time there was activity.
     * The <tt>MessageQueue</tt> relies on this to figure out whether or not to timeout
     */
    public void activity() {
        if (sessionControl != null) sessionControl.activity();
    }

    private boolean isWindowExceeded() {
        return nanoTime() > endWindow;
    }

    private long getEndOfWindow() {
        return endWindow - nanoTime();
    }

    private void descheduleTask() {
        synchronized (activationLock) {
            if (task != null) {
                task.cancel(true);
                task = null;
            }
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
    public BlockingQueue<Message> getQueue() {
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
        return !queueRunning || (!isActive() && (nanoTime() - lastTransmission) > TIMEOUT);
    }

    /**
     * Returns true if the queue is currently active and polling
     *
     * @return true if the queue is actively polling
     */
    public boolean isActive() {
        return lock.availablePermits() == 0;
    }


    public boolean isInitialized() {
        return !initLock;
    }

    /**
     * Fakes a transmission, shows life with a heartbeat
     */
    public void heartBeat() {
        lastTransmission = nanoTime();
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

    public void finishInit() {
        initLock = false;
    }

    /**
     * Stops the queue, closes it on the bus and clears it completely
     */
    public void stopQueue() {
        queue.offer(new QueueStopMessage());
    }

    private static long secs(long secs) {
        return secs * 1000000000;
    }

    private static long millis(long millis) {
        return millis * 1000000;
    }
}
