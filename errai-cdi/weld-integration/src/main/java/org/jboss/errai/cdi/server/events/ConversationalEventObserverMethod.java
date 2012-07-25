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

package org.jboss.errai.cdi.server.events;

import static org.jboss.errai.enterprise.client.cdi.api.CDI.getSubjectNameByType;

import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RoutingFlag;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

/**
 * An implementation of the the CDI SPI {@code ObserverMethod} interface which is used to intercept events within the
 * CDI container. The purpose of this implementation is to observe an event which is exposed to the bus and
 * transmit the event "conversationally" to the current client scope </p>
 *
 * @author Mike Brock
 */
public class ConversationalEventObserverMethod extends EventObserverMethod {
  public ConversationalEventObserverMethod(final EventRoutingTable eventRoutingTable,
                                           final Class<?> type,
                                           final MessageBus bus,
                                           final Annotation... qualifiers) {
    super(eventRoutingTable, type, bus, qualifiers);
  }

  @Override
  public void notify(Object event) {
    final EventConversationContext.Context ctx = EventConversationContext.get();
    if (ctx != null && ctx.getSessionId() != null) {
      if (ctx.getEventObject() == event) return;

      final Map<String, Object> messageParts = new HashMap<String, Object>(20);
      messageParts.put(MessageParts.ToSubject.name(), getSubjectNameByType(event.getClass().getName()));
      messageParts.put(MessageParts.CommandType.name(), CDICommands.CDIEvent.name());
      messageParts.put(CDIProtocol.BeanType.name(), event.getClass().getName());
      messageParts.put(CDIProtocol.BeanReference.name(), event);
      messageParts.put(MessageParts.SessionID.name(), ctx.getSessionId());

      if (!qualifierForWire.isEmpty()) {
        messageParts.put(CDIProtocol.Qualifiers.name(), qualifierForWire);
      }

      bus.send(CommandMessage.createWithParts(messageParts, RoutingFlag.NonGlobalRouting.flag()));
    }
  }
}
