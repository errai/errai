package org.jboss.errai.bus.server;

/**
 * <tt>MessageQueueExpired</tt> extends the <tt>RuntimeException</tt>. It is thrown when a message has timed out, or
 * expired in some way
 */
public class MessageQueueExpired extends RuntimeException {
    public MessageQueueExpired(String message) {
        super(message);
    }

    public MessageQueueExpired(String message, Throwable cause) {
        super(message, cause);   
    }
}
