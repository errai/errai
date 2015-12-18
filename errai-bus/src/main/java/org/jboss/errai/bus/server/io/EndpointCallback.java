/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.io;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.mvel2.DataConversion;

import java.lang.reflect.Method;
import java.util.List;

/**
 * <tt>EndpointCallback</tt> is a callback function for a message being sent. It invokes the endpoint function
 * specified
 */
public class EndpointCallback implements MessageCallback {
  private final Object genericSvc;
  private final Class[] targetTypes;
  private final Method method;

  /**
   * Initializes the service and endpoint method
   *
   * @param genericSvc
   *         - the service that delivers the message
   * @param method
   *         - the endpoint function
   */
  public EndpointCallback(final Object genericSvc, final Method method) {
    this.genericSvc = genericSvc;
    this.targetTypes = (this.method = method).getParameterTypes();
  }

  /**
   * Invokes the endpoint function based on the details of the message
   *
   * @param message
   *         - the message
   */
  @SuppressWarnings("unchecked")
  public void callback(final Message message) {
    final List<Object> parms = message.get(List.class, "MethodParms");

    if ((parms == null && targetTypes.length != 0) || (parms.size() != targetTypes.length)) {
      throw new MessageDeliveryFailure("wrong number of arguments sent to endpoint. (received: "
              + (parms == null ? 0 : parms.size()) + "; required: " + targetTypes.length + ")");
    }
    else if (parms != null) {
      for (int i = 0; i < parms.size(); i++) {
        final Object p = parms.get(i);

        if (p != null && !targetTypes[i].isAssignableFrom(p.getClass())) {
          if (DataConversion.canConvert(targetTypes[i], p.getClass())) {
            parms.set(i, DataConversion.convert(p, targetTypes[i]));
          }
          else {
            throw new MessageDeliveryFailure("type mismatch in method parameters");
          }
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
