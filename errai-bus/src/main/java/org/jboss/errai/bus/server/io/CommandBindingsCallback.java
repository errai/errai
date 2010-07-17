package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class CommandBindingsCallback implements MessageCallback {
    private final Map<String, MethodDispatcher> methodDispatchers;

    public CommandBindingsCallback(Map<String, Method> commandBindings, Object delegate) {
        this.methodDispatchers = new HashMap<String, MethodDispatcher>(commandBindings.size() * 2);


        for (Map.Entry<String, Method> entry : commandBindings.entrySet()) {
            Class[] parmTypes = entry.getValue().getParameterTypes();

            if (parmTypes.length > 1 ||
                    (parmTypes.length == 1 && !Message.class.isAssignableFrom(parmTypes[0]))) {
                throw new IllegalStateException("method does not implement signature: " + entry.getValue().getName()
                        + "(" + Message.class.getName() + ")");
            }

            methodDispatchers.put(entry.getKey(),
                    parmTypes.length == 0 ?
                            new NoParamMethodDispatcher(delegate, entry.getValue()) :
                            new DefaultMethodDispatcher(delegate, entry.getValue()));
        }

    }

    public void callback(Message message) {
        MethodDispatcher method = methodDispatchers.get(message.getCommandType());
        if (method == null) {
            throw new RuntimeException("no such command: " + message.getCommandType());
        } else {
            try {
                method.dispatch(message);
            }
            catch (Exception e) {
                throw new MessageDeliveryFailure(e);
            }
        }
    }

    private abstract class MethodDispatcher {
        protected Object delegate;
        protected Method method;

        protected MethodDispatcher(Object delegate, Method method) {
            this.delegate = delegate;
            this.method = method;
        }

        abstract void dispatch(Message m) throws Exception;
    }

    private class NoParamMethodDispatcher extends MethodDispatcher {
        NoParamMethodDispatcher(Object delegate, Method method) {
            super(delegate, method);
        }

        @Override
        void dispatch(Message m) throws Exception {
            method.invoke(delegate);
        }
    }

    private class DefaultMethodDispatcher extends MethodDispatcher {
        DefaultMethodDispatcher(Object delegate, Method method) {
            super(delegate, method);
        }

        @Override
        void dispatch(Message m) throws Exception {
            method.invoke(delegate, m);
        }
    }
}
