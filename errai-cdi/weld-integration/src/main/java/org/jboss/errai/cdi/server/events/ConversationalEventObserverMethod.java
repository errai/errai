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

import java.lang.annotation.Annotation;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RoutingFlag;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * An implementation of the the CDI SPI {@code ObserverMethod} interface which is used to intercept events within the
 * CDI container. The purpose of this implementation is to observe an event which is exposed to the bus and
 * transmit the event "conversationally" to the current client scope </p>
 *
 * @author Mike Brock
 */
public class ConversationalEventObserverMethod extends EventObserverMethod {
  public ConversationalEventObserverMethod(Class<?> type, MessageBus bus, Annotation... qualifiers) {
    super(type, bus, qualifiers);
  }

  @Override
  public void notify(Object event) {
    if (!type.equals(event.getClass())) return;

    EventConversationContext.Context ctx = EventConversationContext.get();
    if (ctx != null && ctx.getSession() != null) {
      if (ctx.getEventObject() == event) return;

      if (!qualifierForWire.isEmpty()) {
        MessageBuilder.createMessage().toSubject(CDI.CLIENT_DISPATCHER_SUBJECT).command(CDICommands.CDIEvent)
                .with(MessageParts.SessionID.name(), ctx.getSession())
                .with(CDIProtocol.BeanType, type.getName()).with(CDIProtocol.Qualifiers, qualifierForWire)
                .with(CDIProtocol.BeanReference, event)
                .flag(RoutingFlag.NonGlobalRouting).noErrorHandling().sendNowWith(bus);
      }
      else {
        MessageBuilder.createMessage().toSubject(CDI.CLIENT_DISPATCHER_SUBJECT).command(CDICommands.CDIEvent)
                .with(MessageParts.SessionID.name(), ctx.getSession())
                .with(CDIProtocol.BeanType, type.getName()).with(CDIProtocol.BeanReference, event)
                .flag(RoutingFlag.NonGlobalRouting).noErrorHandling()
                .sendNowWith(bus);
      }
    }
  }
}
