package org.jboss.errai.server.bus;

public class MessageDeliveryFailure extends RuntimeException {
    public MessageDeliveryFailure() {
    }

    public MessageDeliveryFailure(String message) {
        super(message);
    }

    public MessageDeliveryFailure(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageDeliveryFailure(Throwable cause) {
        super(cause);
    }
}
