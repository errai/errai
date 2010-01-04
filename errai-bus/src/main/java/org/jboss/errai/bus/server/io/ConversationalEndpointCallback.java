package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.ConversationMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.MessageDeliveryFailure;
import org.jboss.errai.bus.server.util.ErrorHelper;
import org.mvel2.DataConversion;

import java.lang.reflect.Method;

import static org.mvel2.DataConversion.canConvert;
import static org.mvel2.DataConversion.convert;

public class ConversationalEndpointCallback implements MessageCallback {
    private Object genericSvc;
    private Class[] targetTypes;
    private Method method;
    private MessageBus bus;

    public ConversationalEndpointCallback(Object genericSvc, Method method, MessageBus bus) {
        this.genericSvc = genericSvc;
        this.targetTypes = (this.method = method).getParameterTypes();
        this.bus = bus;
    }

    public void callback(Message message) {
        Object[] parms = message.get(Object[].class, "MethodParms");

        if ((parms == null && targetTypes.length != 0) || (parms.length != targetTypes.length)) {
            throw new MessageDeliveryFailure("wrong number of arguments sent to endpoint. (received: "
                    + (parms == null ? 0 : parms.length) + "; required: " + targetTypes.length + ")");
        }
        for (int i = 0; i < parms.length; i++) {
            if (parms[i] != null && !targetTypes[i].isAssignableFrom(parms[i].getClass())) {
                if (canConvert(targetTypes[i], parms[i].getClass())) {
                    parms[i] = convert(parms[i], targetTypes[i]);
                } else {
                    throw new MessageDeliveryFailure("type mismatch in method parameters");
                }
            }
        }

        try {
            ConversationMessage.create(message)
                    .set("MethodReply", method.invoke(genericSvc, parms))
                    .sendNowWith(bus);
        }
        catch (Exception e) {
            throw new MessageDeliveryFailure("error invoking endpoint", e);
        }
    }
}