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

import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.Payload;

import static java.lang.System.currentTimeMillis;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageQueue {
    private static final long TIMEOUT = Boolean.getBoolean("org.jboss.errai.debugmode") ? (1000 * 60 * 60) : (1000 * 46);
    private static final int MAXIMUM_PAYLOAD_SIZE = 10;
    private static final long DEFAULT_TRANSMISSION_WINDOW = 25;
    private static final long MAX_TRANSMISSION_WINDOW = 100;

    private long transmissionWindow = 25;
    private long lastTransmission = currentTimeMillis();
    private long lastEnqueue = currentTimeMillis();
    private long endWindow;

    private int lastQueueSize = 0;
    private boolean throttleIncoming = false;

    private volatile boolean pollActive = false;

    private boolean _windowPolling = false;
    private boolean windowPolling = false;

    private QueueActivationCallback activationCallback;
    private BlockingQueue<Message> queue;

    public MessageQueue(int queueSize) {
        this.queue = new LinkedBlockingQueue<Message>(queueSize);
    }

    public Payload poll(boolean wait) {
        try {
            Message m;
            if (wait) {
                if (pollActive) {
                    throw new RuntimeException("concurrent polling not allowed!");
                }
                pollActive = true;
                m = queue.poll(45, TimeUnit.SECONDS);
                pollActive = false;
            } else {
                m = queue.poll();
            }

            long startWindow = currentTimeMillis();
            int payLoadSize = 0;

            Payload p = new Payload(m == null ? heartBeat : m);

            if (_windowPolling) {
                windowPolling = true;
                _windowPolling = false;
            } else if (windowPolling) {
           //     long endWindow = startWindow + transmissionWindow;
                while (!queue.isEmpty() && payLoadSize < MAXIMUM_PAYLOAD_SIZE
                        && !isWindowExceeded()) {
                    p.addMessage(queue.poll());
                    payLoadSize++;

                    try {
                        if (queue.isEmpty())
                            Thread.sleep(currentTimeMillis() - endWindow);
                    }
                    catch (Exception e) {
                        // just resume.
                    }
                }

                if (!throttleIncoming && queue.size() > lastQueueSize) {
                    if (transmissionWindow < MAX_TRANSMISSION_WINDOW) {
                        transmissionWindow += 5;
                        System.err.println("Congestion on queue -- New transmission window: " + transmissionWindow + "]");
                    } else {
                        throttleIncoming = true;
                        System.err.println("[Warning: A queue has become saturated and performance is now being degraded.]");
                    }

                } else if (queue.isEmpty()) {
                    transmissionWindow = DEFAULT_TRANSMISSION_WINDOW;
                    throttleIncoming = false;
                }
            }

            lastTransmission = currentTimeMillis();
            lastQueueSize = queue.size();
            endWindow = lastTransmission + transmissionWindow;

            return p;
        }
        catch (InterruptedException e) {
            e.printStackTrace();
            return new Payload(heartBeat);
        }
    }

    public boolean offer(final Message message) {
        boolean b = false;
        lastEnqueue = currentTimeMillis();
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
            activationCallback.activate(this);
        }

        return b;
    }

    private boolean isWindowExceeded() {
        return currentTimeMillis() > endWindow;
    }

    public boolean messagesWaiting() {
        return !queue.isEmpty();
    }

    public void setActivationCallback(QueueActivationCallback activationCallback) {
        this.activationCallback = activationCallback;
    }

    public QueueActivationCallback getActivationCallback() {
        return activationCallback;
    }

    public BlockingQueue<Message> getQueue() {
        return queue;
    }

    public boolean isStale() {
        return !pollActive && (currentTimeMillis() - lastTransmission) > TIMEOUT;
    }

    public boolean isActive() {
        return pollActive;
    }

    public void heartBeat() {
        lastTransmission = System.currentTimeMillis();
    }

    public boolean isWindowPolling() {
        return windowPolling;
    }

    public void setWindowPolling(boolean windowPolling) {
        this._windowPolling = windowPolling;
    }

    private static final Message heartBeat = new Message() {
        public String getSubject() {
            return "HeartBeat";
        }

        public Object getMessage() {
            return null;
        }
    };
}
