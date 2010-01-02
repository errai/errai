package org.jboss.errai.bus.server;

public class MessageQueueExpired extends RuntimeException {
    public MessageQueueExpired(String message) {
        super(message);
    }

    public MessageQueueExpired(String message, Throwable cause) {
        super(message, cause);   
    }
}
