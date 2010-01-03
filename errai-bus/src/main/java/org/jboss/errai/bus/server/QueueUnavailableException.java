package org.jboss.errai.bus.server;

public class QueueUnavailableException extends RuntimeException {
    public QueueUnavailableException(String message) {
        super(message);
    }

    public QueueUnavailableException(String message, Throwable cause) {
        super(message, cause);  
    }
}
