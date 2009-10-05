package org.jboss.errai.workspaces.server.bus;

public class NoSubscribersToDeliverTo extends MessageDeliveryFailure {
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
