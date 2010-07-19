package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;

import java.lang.reflect.Method;

/**
 * User: christopherbrock
 * Date: 19-Jul-2010
 * Time: 4:57:59 PM
 */
public class MethodEndpointCallback implements MessageCallback {
    private Object instance;
    private Method method;

    public MethodEndpointCallback(Object instance, Method method) {
        this.instance = instance;
        this.method = method;
    }

    public void callback(Message message) {
        try {
            method.invoke(instance, message);
        }
        catch (Exception e) {
            throw new MessageDeliveryFailure(e);
        }
    }
}
