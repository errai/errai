package org.jboss.errai.bus.server;

public class NoSubscribersToDeliverTo extends MessageDeliveryFailure {
    private static final long serialVersionUID = -5385972750788483158L;

    public NoSubscribersToDeliverTo() {
        super();
    }

    public NoSubscribersToDeliverTo(String message) {
        super(message);
    }

    public NoSubscribersToDeliverTo(String message, Throwable cause) {
        super(message, cause);    
    }

    public NoSubscribersToDeliverTo(Throwable cause) {
        super(cause);
    }
}
