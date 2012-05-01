/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.RoutingFlag;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.cdi.server.CDIServerUtil;
import org.jboss.errai.cdi.server.ScopeUtil;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * Acts as a bridge between Errai Bus and the CDI event system.<br/>
 * Includes marshalling/unmarshalling of event types.
 */
public class EventDispatcher implements MessageCallback {
  private static final String CDI_EVENT_CHANNEL_OPEN = "cdi.event.channel.open";
  private static final String CDI_REMOTE_EVENTS_ACTIVE = "cdi.event.active.events";

  private BeanManager beanManager;

  private Set<String> observedEvents;
  private Map<String, Annotation> allQualifiers;

  public EventDispatcher(BeanManager beanManager, Set<String> observedEvents,
                         Map<String, Annotation> qualifiers) {
    this.beanManager = beanManager;
    this.observedEvents = observedEvents;
    this.allQualifiers = qualifiers;
  }

  public void callback(final Message message) {
    try {
      ScopeUtil.associateRequestContext(message);

      /**
       * If the message didn't not come from a remote, we don't handle it.
       */
      if (!message.isFlagSet(RoutingFlag.FromRemote))
        return;

      final LocalContext localContext = LocalContext.get(message);

      switch (CDICommands.valueOf(message.getCommandType())) {
        case CDIEvent:
          if (!isRoutable(localContext, message)) {
            return;
          }

          final Object o = message.get(Object.class, CDIProtocol.BeanReference);
          EventConversationContext.activate(o, CDIServerUtil.getSession(message));
          try {
            @SuppressWarnings("unchecked")
            final Set<String> qualifierNames = message.get(Set.class, CDIProtocol.Qualifiers);
            List<Annotation> qualifiers = null;
            if (qualifierNames != null) {
              for (String qualifierName : qualifierNames) {
                if (qualifiers == null) {
                  qualifiers = new ArrayList<Annotation>();
                }
                Annotation qualifier = allQualifiers.get(qualifierName);
                if (qualifier != null) {
                  qualifiers.add(qualifier);
                }
              }
            }

            if (qualifiers != null) {
              beanManager.fireEvent(o, qualifiers.toArray(new Annotation[qualifiers.size()]));
            }
            else {
              beanManager.fireEvent(o);
            }
          }
          finally {
            EventConversationContext.deactivate();
          }

          break;

        case AttachRemote:
          MessageBuilder.createConversation(message).toSubject(CDI.CLIENT_DISPATCHER_SUBJECT)
                  .command(BusCommands.RemoteSubscribe)
                  .with(MessageParts.Value, observedEvents.toArray(new String[observedEvents.size()])).done().reply();

          localContext.setAttribute(CDI_EVENT_CHANNEL_OPEN, "1");
          break;

        default:
          throw new IllegalArgumentException("Unknown command type " + message.getCommandType());
      }
    }
    catch (Exception e) {
      throw new RuntimeException("Failed to dispatch CDI Event", e);
    }
  }

  public boolean isRoutable(final LocalContext localContext, final Message message) {
    return "1".equals(localContext.getAttribute(String.class, CDI_EVENT_CHANNEL_OPEN))
            && observedEvents.contains(message.get(String.class, CDIProtocol.BeanType));
  }
}
