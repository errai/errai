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

import static org.jboss.errai.enterprise.client.cdi.api.CDI.getSubjectNameByType;

import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;
import javax.inject.Qualifier;

import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.Conversational;

/**
 * An implementation of the the CDI SPI {@code ObserverMethod} interface which is used to intercept events within the
 * CDI container. The purpose of this implementation is to observe an event which is exposed to the bus and
 * transmit the event to all clients listening to this event.</p>
 * <p/>
 * For the "conversational" version of this, see {@link org.jboss.errai.cdi.server.events.ConversationalEventObserverMethod}.
 *
 * @author Mike Brock
 */
public class DynamicEventObserverMethod implements ObserverMethod {

  /**
   * An instance of the MessageBus.
   */
  protected final EventRoutingTable eventRoutingTable;
  protected final MessageBus bus;
  protected final Class<?> eventType;
  protected final Set<String> annotationTypes;
  protected final Set<Annotation> annotations;

  public DynamicEventObserverMethod(final EventRoutingTable eventRoutingTable,
                                    final MessageBus bus,
                                    final Class<?> eventType,
                                    final Set<String> annotations) throws ClassNotFoundException {

    this.eventRoutingTable = eventRoutingTable;
    this.bus = bus;
    this.eventType = eventType;
    this.annotationTypes = annotations;
    this.annotations = new HashSet<Annotation>();

    for (final String fqcn : annotations) {
      final Class<? extends Annotation> annoType = Class.forName(fqcn).asSubclass(Annotation.class);
      if (annoType.isAnnotationPresent(Qualifier.class)) {
        this.annotations.add(new AnnotationWrapper(annoType));
      }
    }
  }

  @Override
  public Class<?> getBeanClass() {
    return eventType;
  }

  @Override
  public Class<?> getObservedType() {
    return eventType;
  }

  @Override
  public Set<Annotation> getObservedQualifiers() {
    return annotations;
  }

  @Override
  public Reception getReception() {
    return Reception.ALWAYS;
  }

  @Override
  public TransactionPhase getTransactionPhase() {
    return TransactionPhase.IN_PROGRESS;
  }

  @Override
  public void notify(final Object event) {
    if (EventConversationContext.isEventObjectInContext(event)) return;

    final Class<? extends Object> aClass = event.getClass();
    final String sessionId;
    final EventConversationContext.Context ctx = EventConversationContext.get();
    if (aClass.isAnnotationPresent(Conversational.class) && ctx != null && ctx.getSessionId() != null) {
      if (ctx.getEventObject() == event) return;
      sessionId = ctx.getSessionId();
    }
    else {
      sessionId = null;
    }

    final Map<String, Object> messageParts = new HashMap<String, Object>(10);
    messageParts.put(MessageParts.ToSubject.name(), getSubjectNameByType(aClass.getName()));
    messageParts.put(MessageParts.CommandType.name(), CDICommands.CDIEvent.name());
    messageParts.put(CDIProtocol.BeanType.name(), aClass.getName());
    messageParts.put(CDIProtocol.BeanReference.name(), event);


    if (!annotationTypes.isEmpty()) {
      messageParts.put(CDIProtocol.Qualifiers.name(), annotationTypes);
    }

    if (sessionId != null) {
      messageParts.put(MessageParts.SessionID.name(), sessionId);
      bus.send(CommandMessage.createWithParts(messageParts));
    }
    else {
      for (final String id : eventRoutingTable.getQueueIdsForRoute(event.getClass().getName(), annotationTypes)) {
        bus.send(CommandMessage.createWithParts(new RoutingMap(messageParts, id)));
      }
    }
  }

  static class AnnotationWrapper implements Annotation {
    private final Class<? extends Annotation> annoType;

    AnnotationWrapper(final Class<? extends Annotation> annoType) {
      this.annoType = annoType;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return annoType;
    }
  }

  @Override
  public boolean equals(final Object o) {
    if (this == o) return true;
    if (!(o instanceof DynamicEventObserverMethod)) return false;

    final DynamicEventObserverMethod that = (DynamicEventObserverMethod) o;

    if (annotationTypes != null ? !annotationTypes.equals(that.annotationTypes) : that.annotationTypes != null)
      return false;

    return !(eventType != null ? !eventType.equals(that.eventType) : that.eventType != null);
  }

  @Override
  public int hashCode() {
    int result = eventType != null ? eventType.hashCode() : 0;
    result = 31 * result + (annotationTypes != null ? annotationTypes.hashCode() : 0);
    return result;
  }
}
