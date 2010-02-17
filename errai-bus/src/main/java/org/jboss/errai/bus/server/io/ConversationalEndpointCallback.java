package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.server.MessageDeliveryFailure;

import java.lang.reflect.Method;

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;
import static org.mvel2.DataConversion.canConvert;
import static org.mvel2.DataConversion.convert;

/**
 * <tt>ConversationalEndpointCallback</tt> creates a conversation that invokes an endpoint function
 */
public class ConversationalEndpointCallback implements MessageCallback {
    private Object genericSvc;
    private Class[] targetTypes;
    private Method method;
    private MessageBus bus;

    /**
     * Initializes the service, method and bus
     *
     * @param genericSvc - the service the bus is subscribed to
     * @param method - the endpoint function
     * @param bus - the bus to send the messages on
     */
    public ConversationalEndpointCallback(Object genericSvc, Method method, MessageBus bus) {
        this.genericSvc = genericSvc;
        this.targetTypes = (this.method = method).getParameterTypes();
        this.bus = bus;
    }

    /**
     * Callback function. Creates the conversation and invokes the endpoint using the message specified
     *
     * @param message - the message to initiate the conversation
     */
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
            createConversation(message)
                    .subjectProvided().signalling()
                    .with("MethodReply", method.invoke(genericSvc, parms))
                    .noErrorHandling().sendNowWith(bus);
       
        }
        catch (Exception e) {
            throw new MessageDeliveryFailure("error invoking endpoint", e);
        }
    }
}