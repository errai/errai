package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.server.MessageDeliveryFailure;

import java.util.Collections;
import java.util.Map;

/**
 * <tt>RemoteServiceCallback</tt> implements callback functionality for a remote service. It invokes the callback
 * functions for all endpoints specified
 */
public class RemoteServiceCallback implements MessageCallback {
    private final Map<String, MessageCallback> endpoints;

    /**
     * Initializes the <tt>RemoteServiceCallback</tt> with a set of endpoints and their callback functions
     *
     * @param endpoints - Map of endpoints to their callback function
     */
    public RemoteServiceCallback(Map<String, MessageCallback> endpoints) {
        this.endpoints = Collections.unmodifiableMap(endpoints);
    }

    /**
     * Invokes all callback functions that can be associated to the <tt>message</tt>
     *
     * @param message - the message in question
     */
    public void callback(Message message) {
        if (!endpoints.containsKey(message.getCommandType())) {
            throw new MessageDeliveryFailure("no such endpoint '" + message.getCommandType() + "' in service: " + message.getSubject());
        }
        endpoints.get(message.getCommandType()).callback(message);
    }
}
