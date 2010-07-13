package org.jboss.errai.bus.client.api.base;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;

public class ServiceCanceller implements MessageCallback {
    private String serviceName;
    private MessageBus bus;

    public ServiceCanceller(String serviceName, MessageBus bus) {
        this.serviceName = serviceName;
        this.bus = bus;
    }

    public void callback(Message message) {
        bus.unsubscribeAll(serviceName);
    }
}
