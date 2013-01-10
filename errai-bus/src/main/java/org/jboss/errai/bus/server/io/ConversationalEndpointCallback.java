/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

import static org.jboss.errai.bus.client.api.base.MessageBuilder.createConversation;
import static org.mvel2.DataConversion.canConvert;
import static org.mvel2.DataConversion.convert;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.api.RpcContext;

/**
 * <tt>ConversationalEndpointCallback</tt> creates a conversation that invokes an endpoint function
 */
public class ConversationalEndpointCallback implements MessageCallback {
  private final ServiceInstanceProvider serviceProvider;
  private final Class[] targetTypes;
  private final Method method;
  private final MessageBus bus;

  /**
   * Initializes the service, method and bus
   *
   * @param genericSvc - the service the bus is subscribed to
   * @param method     - the endpoint function
   * @param bus        - the bus to send the messages on
   */
  public ConversationalEndpointCallback(final ServiceInstanceProvider genericSvc,
                                        final Method method,
                                        final MessageBus bus) {
    this.serviceProvider = genericSvc;
    this.targetTypes = (this.method = method).getParameterTypes();
    this.bus = bus;
  }

  /**
   * Callback function. Creates the conversation and invokes the endpoint using the message specified
   *
   * @param message - the message to initiate the conversation
   */
  @Override
  @SuppressWarnings({"unchecked"})
  public void callback(Message message) {
    final List<Object> parms = message.get(List.class, "MethodParms");

    if ((parms == null && targetTypes.length != 0) || (parms.size() != targetTypes.length)) {
      throw new MessageDeliveryFailure(
              "wrong number of arguments sent to endpoint. (received: "
              + (parms == null ? 0 : parms.size())
                      + "; required: " + targetTypes.length + ")");
    }
    for (int i = 0; i < parms.size(); i++) {
       Object p = parms.get(i);

      if (p != null && !targetTypes[i].isAssignableFrom(p.getClass())) {
        if (canConvert(targetTypes[i], p.getClass())) {
          p = convert(p, targetTypes[i]);
        }
        else if (targetTypes[i].isArray()) {
          if (p instanceof Collection) {
            final Collection c = (Collection) p;
            p = c.toArray((Object[]) Array.newInstance(targetTypes[i].getComponentType(), c.size()));
          }
          else if (p.getClass().isArray()) {
            final int length = Array.getLength(p);
            final Class toComponentType = p.getClass().getComponentType();
            final Object parmValue = p;
            final Object newArray = Array.newInstance(targetTypes[i].getComponentType(), length);

            for (int x = 0; x < length; x++) {
              Array.set(newArray, x, convert(Array.get(parmValue, x), toComponentType));
            }

            p = newArray;
          }
        }
        else {
          throw new MessageDeliveryFailure("type mismatch in method parameters " +
                  " (got types: "
                  + Arrays.toString(getTypesFrom(parms))
                  + "; but expected: "
                  + Arrays.toString(targetTypes)
                  + ")");
        }
      }
    }

    try {
      RpcContext.set(message);
      final Object methReply = method.invoke(serviceProvider.get(message), parms.toArray(new Object[parms.size()]));

      if (method.getReturnType().equals(void.class)) {
        createConversation(message)
                .subjectProvided()
                .noErrorHandling().sendNowWith(bus);
      }
      else {
        createConversation(message)
                .subjectProvided()
                .with("MethodReply", methReply)
                .noErrorHandling().sendNowWith(bus);
      }
    }
    catch (MessageDeliveryFailure e) {
      throw e;
    }
    catch (InvocationTargetException e) {
      throw new MessageDeliveryFailure("error invoking endpoint", e.getCause());
    }
    catch (Exception e) {
      throw new MessageDeliveryFailure("error invoking endpoint", e);
    }
    finally {
      RpcContext.remove();
    }
  }

  private static Class[] getTypesFrom(final List<Object> objects) {
    if (objects == null) return new Class[0];

    final Class[] types = new Class[objects.size()];
    for (int i = 0, objectsLength = objects.size(); i < objectsLength; i++) {
      final Object o = objects.get(i);
      if (o == null) {
        types[i] = Object.class;
      }
      else {
        types[i] = o.getClass();
      }
    }

    return types;
  }
}