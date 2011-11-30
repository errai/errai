/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.mvel2.DataConversion;

import java.lang.reflect.Method;

/**
 * <tt>EndpointCallback</tt> is a callback function for a message being sent. It invokes the endpoint function
 * specified
 */
public class EndpointCallback implements MessageCallback {
  private Object genericSvc;
  private Class[] targetTypes;
  private Method method;

  /**
   * Initializes the service and endpoint method
   *
   * @param genericSvc - the service that delivers the message
   * @param method     - the endpoint function
   */
  public EndpointCallback(Object genericSvc, Method method) {
    this.genericSvc = genericSvc;
    this.targetTypes = (this.method = method).getParameterTypes();
  }

  /**
   * Invokes the endpoint function based on the details of the message
   *
   * @param message - the message
   */
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
        }
        else {
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
