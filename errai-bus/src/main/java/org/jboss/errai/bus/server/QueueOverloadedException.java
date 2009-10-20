package org.jboss.errai.bus.server;

public class QueueOverloadedException extends MessageDeliveryFailure {
    private static final long serialVersionUID = 6014530858847384745L;

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
