package org.jboss.errai.server.bus;

public class QueueOverloadedException extends MessageDeliveryFailure {
    public QueueOverloadedException() {
    }

    public QueueOverloadedException(String message) {
        super(message);
    }

    public QueueOverloadedException(String message, Throwable cause) {
        super(message, cause);
    }

    public QueueOverloadedException(Throwable cause) {
        super(cause);
    }
}
