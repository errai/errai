package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.MessageDeliveryFailure;

import java.util.Map;

public class RemoteServiceCallback implements MessageCallback {
    private final Map<String, MessageCallback> endpoints;

    public RemoteServiceCallback(Map<String, MessageCallback> endpoints) {
        this.endpoints = endpoints;
    }

    public void callback(Message message) {
        if (!endpoints.containsKey(message.getCommandType())) {
            throw new MessageDeliveryFailure("no such endpoint '" + message.getCommandType() + "' in service: " + message.getSubject());
        }
        endpoints.get(message.getCommandType()).callback(message);
    }
}
