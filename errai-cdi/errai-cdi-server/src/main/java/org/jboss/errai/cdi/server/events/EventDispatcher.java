/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
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

import static org.jboss.errai.enterprise.client.cdi.api.CDI.getSubjectNameByType;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.EventMetadata;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.cdi.server.CDIServerUtil;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.config.rebind.EnvUtil;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Acts as a bridge between Errai Bus and the CDI event system.
 *
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class EventDispatcher implements MessageCallback {
  private static final Logger log = LoggerFactory.getLogger("EventDispatcher");
  private static final String CDI_EVENT_CHANNEL_OPEN = "cdi.event.channel.open";

  private final BeanManager beanManager;
  private final EventRoutingTable eventRoutingTable;
  private final MessageBus messagebus;
  private final Set<String> observedEvents;
  private final Map<String, Annotation> eventQualifiers;

  private final Set<ClientObserverMetadata> clientObservers = Collections
          .newSetFromMap(new ConcurrentHashMap<ClientObserverMetadata, Boolean>());

  public EventDispatcher(final BeanManager beanManager, final EventRoutingTable eventRoutingTable,
          final MessageBus messageBus, final Set<String> observedEvents, final Map<String, Annotation> eventQualifiers) {

    this.beanManager = beanManager;
    this.eventRoutingTable = eventRoutingTable;
    this.messagebus = messageBus;
    this.observedEvents = observedEvents;
    this.eventQualifiers = eventQualifiers;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void callback(final Message message) {
    if (!message.isFlagSet(RoutingFlag.FromRemote))
      return;

    try {
      final LocalContext localContext = LocalContext.get(message);
      final String typeName = message.get(String.class, CDIProtocol.BeanType);
      final Set<String> annotationTypes = message.get(Set.class, CDIProtocol.Qualifiers);

      switch (CDICommands.valueOf(message.getCommandType())) {
      case RemoteSubscribe:
        final Class<?> type = Thread.currentThread().getContextClassLoader().loadClass(typeName);
        final ClientObserverMetadata clientObserver = new ClientObserverMetadata(type, annotationTypes);

        if (!clientObservers.contains(clientObserver)) {
          if (type == null || !EnvUtil.isPortableType(type)) {
            log.warn("client tried to register a non-portable type: " + type);
            return;
          }
          clientObservers.add(clientObserver);
        }
        eventRoutingTable.activateRoute(typeName, annotationTypes, message.getResource(QueueSession.class, "Session"));
        break;

      case RemoteUnsubscribe:
        eventRoutingTable.deactivateRoute(typeName, annotationTypes, message.getResource(QueueSession.class, "Session"));
        break;

      case CDIEvent:
        if (!isRoutable(localContext, message)) {
          return;
        }

        final Object o = message.get(Object.class, CDIProtocol.BeanReference);
        EventConversationContext.activate(o, CDIServerUtil.getSession(message));
        try {
          final Set<String> qualifierNames = message.get(Set.class, CDIProtocol.Qualifiers);
          List<Annotation> qualifiers = new ArrayList<Annotation>();

          if (qualifierNames != null) {
            for (final String serializedQualifier : qualifierNames) {
              final Annotation qualifier = eventQualifiers.get(serializedQualifier);
              if (qualifier != null) {
                qualifiers.add(qualifier);
              }
            }
          }
          // Fire event to all local observers
          Annotation[] qualArray = qualifiers.toArray(new Annotation[qualifiers.size()]);
          Set<ObserverMethod<? super Object>> observerMethods = beanManager.resolveObserverMethods(o, qualArray);
          for (ObserverMethod<? super Object> observer : observerMethods) {
            // Don't mirror the event back to the clients
            if (!(AnyEventObserver.class.equals(observer.getBeanClass()))) {
              observer.notify(o);
            }
          }
        } finally {
          EventConversationContext.deactivate();
        }

        break;

      case AttachRemote:
        if (observedEvents.size() > 0) {
          MessageBuilder.createConversation(message).toSubject(CDI.CLIENT_DISPATCHER_SUBJECT)
                  .command(CDICommands.AttachRemote).with(MessageParts.RemoteServices, getEventTypes()).done().reply();
        }
        else {
          MessageBuilder.createConversation(message).toSubject(CDI.CLIENT_DISPATCHER_SUBJECT)
                  .command(CDICommands.AttachRemote).with(MessageParts.RemoteServices, "").done().reply();
        }

        localContext.setAttribute(CDI_EVENT_CHANNEL_OPEN, "1");
        break;

      default:
        throw new IllegalArgumentException("Unknown command type " + message.getCommandType());
      }
    } catch (Exception e) {
      throw new RuntimeException("Failed to dispatch CDI Event", e);
    }
  }

  private String getEventTypes() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final String s : observedEvents) {

      if (stringBuilder.length() != 0) {
        stringBuilder.append(",");
      }
      stringBuilder.append(s);
    }
    return stringBuilder.toString();
  }

  public boolean isRoutable(final LocalContext localContext, final Message message) {
    return "1".equals(localContext.getAttribute(String.class, CDI_EVENT_CHANNEL_OPEN))
            && observedEvents.contains(message.get(String.class, CDIProtocol.BeanType));
  }

  public void sendEventToClients(Object event, EventMetadata emd) {
    for (ClientObserverMetadata clientObserver : clientObservers) {
      if (clientObserver.matches(event, emd)) {
        sendEventToClient(event, clientObserver.getQualifiers());
      }
    }
  }

  private void sendEventToClient(Object event, Set<String> qualifierTypes) {
    final Class<? extends Object> eventType = event.getClass();
    final String sessionId;
    final EventConversationContext.Context ctx = EventConversationContext.get();
    if (eventType.isAnnotationPresent(Conversational.class) && ctx != null && ctx.getSessionId() != null) {
      sessionId = ctx.getSessionId();
    }
    else {
      sessionId = null;
    }

    final Map<String, Object> messageParts = new HashMap<String, Object>(10);
    messageParts.put(MessageParts.ToSubject.name(), getSubjectNameByType(eventType.getName()));
    messageParts.put(MessageParts.CommandType.name(), CDICommands.CDIEvent.name());
    messageParts.put(CDIProtocol.BeanType.name(), eventType.getName());
    messageParts.put(CDIProtocol.BeanReference.name(), event);

    if (!qualifierTypes.isEmpty()) {
      messageParts.put(CDIProtocol.Qualifiers.name(), qualifierTypes);
    }

    if (sessionId != null) {
      messageParts.put(MessageParts.SessionID.name(), sessionId);
      messagebus.send(CommandMessage.createWithParts(messageParts));
    }
    else {
      for (final String id : eventRoutingTable.getQueueIdsForRoute(eventType.getName(), qualifierTypes)) {
        messagebus.send(CommandMessage.createWithParts(new RoutingMap(messageParts, id)));
      }
    }
  }
}
