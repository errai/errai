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

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * A gateway for intercepting {@link ConversationalEventWrapper} instances and then passing them into the bus for
 * transmission to remote clients.
 *
 * @author Mike Brock
 */
@ApplicationScoped
public class ConversationEventGatewayBean {
  public void observesConversationEvents(@Observes ConversationalEventWrapper wrapper) {
    EventConversationContext.Context ctx = EventConversationContext.get();
    if (ctx != null && ctx.getSession() != null) {
      String subject = CDI.getSubjectNameByType(wrapper.getEventType());
      try {
        if (wrapper.getQualifierStrings() != null && !wrapper.getQualifierStrings().isEmpty()) {
          MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
                  .with(MessageParts.SessionID.name(), ctx.getSession())
                  .with(CDIProtocol.TYPE, wrapper.getEventType().getName()).with(CDIProtocol.QUALIFIERS, wrapper.getQualifierStrings())
                  .with(CDIProtocol.OBJECT_REF, wrapper.getEventObject())
                  .flag(RoutingFlags.NonGlobalRouting).noErrorHandling().sendNowWith(wrapper.getBus());
        }
        else {
          MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
                  .with(MessageParts.SessionID.name(), ctx.getSession())
                  .with(CDIProtocol.TYPE,wrapper.getEventType().getName()).with(CDIProtocol.OBJECT_REF, wrapper.getEventObject())
                  .flag(RoutingFlags.NonGlobalRouting).noErrorHandling()
                  .sendNowWith(wrapper.getBus());
        }
      }
      finally {
        EventConversationContext.deactivate();
      }
    }
  }
}
