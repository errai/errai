/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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
import org.jboss.errai.bus.client.api.ResourceProvider;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.base.MessageDeliveryFailure;
import org.jboss.errai.bus.client.api.base.Reply;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.MessageParts;

import java.lang.reflect.Method;

/**
 * User: christopherbrock
 * Date: 19-Jul-2010
 * Time: 4:57:59 PM
 */
public class MethodEndpointDynamicParmCallback implements MessageCallback {
  private final Object instance;
  private final Method method;

  private final String[] parms;
  private final ParmType[] callPlan;

  public MethodEndpointDynamicParmCallback(Object instance, Method method, String[] parms, Class[] parmTypes) {
    this.instance = instance;
    this.method = method;
    this.parms = parms;

    this.callPlan = new ParmType[parmTypes.length];

    for (int i = 0; i < parmTypes.length; i++) {
      if (Message.class.isAssignableFrom(parmTypes[i])) {
        callPlan[i] = ParmType.Message;
      } else if (Reply.class.isAssignableFrom(parmTypes[i])) {
        callPlan[i] = ParmType.Conversation;
      } else {
        callPlan[i] = ParmType.Object;
      }
    }
  }

  public void callback(final Message message) {
    try {
      Object[] parmValues = new Object[parms.length];
      for (int i = 0; i < parmValues.length; i++) {
        switch (callPlan[i]) {
          case Object:
            parmValues[i] = message.get(Object.class, parms[i]);
            break;
          case Message:
            parmValues[i] = message;
            break;
          case Conversation:
            parmValues[i] = new Reply() {
              final Message replyMessage = MessageBuilder.createConversation(message).getMessage();

              public void setValue(Object value) {
                replyMessage.set(MessageParts.Value, value);
              }

              public void set(String part, Object value) {
                replyMessage.set(part, value);
              }

              public void set(Enum<?> part, Object value) {
                replyMessage.set(part, value);
              }

              public void setProvidedPart(String part, ResourceProvider provider) {
                replyMessage.setProvidedPart(part, provider);
              }

              public void setProvidedPart(Enum<?> part, ResourceProvider provider) {
                replyMessage.setProvidedPart(part, provider);
              }

              public void reply() {
                replyMessage.sendNowWith((RequestDispatcher) message.getResource(ResourceProvider.class,
                        RequestDispatcher.class.getName()).get());
              }
            };
            break;
        }
      }

      method.invoke(instance, parmValues);
    } catch (Exception e) {
      throw new MessageDeliveryFailure(e);
    }
  }

  enum ParmType {
    Object,
    Message,
    Conversation
  }
}
