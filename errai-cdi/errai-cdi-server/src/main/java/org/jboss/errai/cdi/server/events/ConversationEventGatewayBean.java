/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.server.events;

import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import java.util.HashMap;
import java.util.Map;

/**
 * A gateway for intercepting {@link ConversationalEventWrapper} instances and then passing them into the bus for
 * transmission to remote clients.
 *
 * @author Mike Brock
 */
@ApplicationScoped
public class ConversationEventGatewayBean {
  public void observesConversationEvents(@Observes ConversationalEventWrapper wrapper) {
    final EventConversationContext.Context ctx = EventConversationContext.get();
    if (ctx != null && ctx.getSessionId() != null) {
      final Map<String, Object> messageParts = new HashMap<String, Object>(20);
      messageParts.put(MessageParts.ToSubject.name(), CDI.getSubjectNameByType(wrapper.getEventType().getName()));
      messageParts.put(MessageParts.CommandType.name(), CDICommands.CDIEvent.name());
      messageParts.put(CDIProtocol.BeanType.name(), wrapper.getEventObject().getClass().getName());
      messageParts.put(CDIProtocol.BeanReference.name(), wrapper.getEventObject());

      messageParts.put(MessageParts.SessionID.name(), ctx.getSessionId());

      try {
        if (wrapper.getQualifierStrings() != null && !wrapper.getQualifierStrings().isEmpty()) {
          messageParts.put(CDIProtocol.Qualifiers.name(), wrapper.getQualifierStrings());
        }

        wrapper.getBus().send(CommandMessage.createWithParts(messageParts, RoutingFlag.NonGlobalRouting.flag()));
      }
      finally {
        EventConversationContext.deactivate();
      }
    }
  }
}
