package org.jboss.errai.bus.server.bus;

import org.jboss.errai.client.bus.Message;

import static java.lang.System.currentTimeMillis;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

public class MessageQueue {
    private static final long TIMEOUT = (1000 * 10);
    private static final int MAXIMUM_PAYLOAD_SIZE = 10;
    private static final long DEFAULT_TRANSMISSION_WINDOW = 25;

    private long transmissionWindow = 25;
    private long lastTransmission = currentTimeMillis();
    private long lastEnqueue = currentTimeMillis();

    private boolean pollActive = false;

    private BlockingQueue<Message> queue;

    public MessageQueue(int queueSize) {
        this.queue = new LinkedBlockingQueue<Message>(queueSize);
    }

    public Payload poll() {
        try {

            pollActive = true;
            Message m = queue.poll(45, TimeUnit.SECONDS);
            pollActive = false;

            long startWindow = currentTimeMillis();
            int payLoadSize = 0;

            Payload p = new Payload(m == null ? heartBeat : m);

            while (!queue.isEmpty() && payLoadSize < MAXIMUM_PAYLOAD_SIZE
                    && (currentTimeMillis() - startWindow) < transmissionWindow) {
                p.addMessage(queue.poll());
                payLoadSize++;
            }

            if ((lastTransmission = currentTimeMillis()) - lastEnqueue > transmissionWindow) {
                transmissionWindow = (lastTransmission - lastEnqueue);
            }
            else {
                transmissionWindow = DEFAULT_TRANSMISSION_WINDOW;
            }

            return p;
        }
        catch (InterruptedException e) {
            return new Payload(heartBeat);
        }
    }

    public boolean offer(final Message message) {
        boolean b = false;
        try {
            b = queue.offer(message, 100, TimeUnit.MILLISECONDS);

            if (!b) {
                throw new QueueOverloadedException("cannot deliver message.");
            }

            lastEnqueue = currentTimeMillis();
            return b;
        }
        catch (InterruptedException e) {
            //todo: create a delivery failure notice.
            if (!b) {
                throw new QueueOverloadedException("cannot deliver message.");
            }
            return b;
        }
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

    private static final Message heartBeat = new Message() {
        public String getSubject() {
            return "HeartBeat";
        }

        public Object getMessage() {
            return null;
        }
    };
}
