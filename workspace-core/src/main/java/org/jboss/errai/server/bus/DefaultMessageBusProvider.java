package org.jboss.errai.server.bus;


public class DefaultMessageBusProvider implements MessageBusProvider {
    private static MessageBus bus;

    public MessageBus getBus() {
        if (bus == null) {
            bus = new MessageBusImpl();
        }
        return bus;
    }

}
