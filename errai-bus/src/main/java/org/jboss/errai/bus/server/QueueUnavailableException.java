package org.jboss.errai.bus.server;

/**
 * <tt>QueueUnavailableException</tt> extends the <tt>RuntimeException</tt>. It is thrown when a message failed to
 * send or be added to the queue, because the queue is not currently active
 */
public class QueueUnavailableException extends RuntimeException {
    public QueueUnavailableException(String message) {
        super(message);
    }

    public QueueUnavailableException(String message, Throwable cause) {
        super(message, cause);  
    }
}
