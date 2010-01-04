package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.server.MessageDeliveryFailure;
import org.mvel2.DataConversion;

import java.lang.reflect.Method;

public class EndpointCallback implements MessageCallback {
    private Object genericSvc;
    private Class[] targetTypes;
    private Method method;

    public EndpointCallback(Object genericSvc, Method method) {
        this.genericSvc = genericSvc;
        this.targetTypes = (this.method = method).getParameterTypes();
    }

    public void callback(Message message) {
        Object[] parms = message.get(Object[].class, "MethodParms");

        if ((parms == null && targetTypes.length != 0) || (parms.length != targetTypes.length)) {
            throw new MessageDeliveryFailure("wrong number of arguments sent to endpoint. (received: "
                    + (parms == null ? 0 : parms.length) + "; required: " + targetTypes.length + ")");
        }
        for (int i = 0; i < parms.length; i++) {
            if (parms[i] != null && !targetTypes[i].isAssignableFrom(parms[i].getClass())) {
                if (DataConversion.canConvert(targetTypes[i], parms[i].getClass())) {
                    parms[i] = DataConversion.convert(parms[i], targetTypes[i]);
                } else {
                    throw new MessageDeliveryFailure("type mismatch in method parameters");
                }
            }
        }

        try {
            method.invoke(genericSvc, parms);
        }
        catch (Exception e) {
            throw new MessageDeliveryFailure("error invoking endpoint", e);
        }
    }
}
