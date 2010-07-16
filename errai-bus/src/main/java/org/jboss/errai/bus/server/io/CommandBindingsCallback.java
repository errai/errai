package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;

import java.lang.reflect.Method;
import java.util.Map;

public class CommandBindingsCallback implements MessageCallback {
    private Object delegate;
    private final Map<String, Method> commandBindings;

    public CommandBindingsCallback(Map<String, Method> commandBindings, Object delegate) {
        this.commandBindings = commandBindings;

        for (Method m : commandBindings.values()) {
            if (m.getParameterTypes().length != 1 || !Message.class.isAssignableFrom(m.getParameterTypes()[0])) {
                throw new IllegalStateException("method does not implement signature: " + m.getName() + "(" + Message.class.getName() + ")");
            }
        }

        this.delegate = delegate;
    }

    public void callback(Message message) {
        Method method = commandBindings.get(message.getCommandType());
        if (method == null) {
            throw new RuntimeException("no such command: " + message.getCommandType());
        } else {
            try {
                method.invoke(delegate, message);
            }
            catch (Exception e) {
                throw new MessageDeliveryFailure(e);
            }
        }
    }
}
