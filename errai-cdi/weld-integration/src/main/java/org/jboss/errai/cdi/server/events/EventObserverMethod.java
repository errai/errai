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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * An implementation of the the CDI SPI {@code ObserverMethod} interface which is used to intercept events within the
 * CDI container. The purpose of this implementation is to observe an event which is exposed to the bus and
 * transmit the event to all clients listening to this event.</p>
 * <p/>
 * For the "conversational" version of this, see {@link ConversationalEventObserverMethod}.
 *
 * @author Mike Brock
 */
public class EventObserverMethod implements ObserverMethod {

  protected final EventRoutingTable eventRoutingTable;

  /**
   * The type of event handled by this ObserverMethod implementation.
   */
  protected final Class<?> type;

  /**
   * A set of the qualifiers that this method observers.
   */
  protected final Set<Annotation> observedQualifiers;

  /**
   * the qualifiers collection to be used for transmitting the data over the wire. This is represented as a List
   * merely for internal wire purposes. It does not connote that ordering matters.
   */
  protected final Set<String> qualifierForWire;

  /**
   * An instance of the MessageBus.
   */
  protected final MessageBus bus;


  public EventObserverMethod(final EventRoutingTable eventRoutingTable,
                             final Class<?> type,
                             final MessageBus bus,
                             final Annotation... qualifiers) {
    this.eventRoutingTable = eventRoutingTable;
    this.type = type;
    this.bus = bus;

    if (qualifiers == null || qualifiers.length == 0) {
      this.observedQualifiers = Collections.emptySet();
      this.qualifierForWire = Collections.emptySet();
    }
    else {
      this.observedQualifiers = Collections.unmodifiableSet(new HashSet<Annotation>(Arrays.asList(qualifiers)));
      this.qualifierForWire = CDI.getQualifiersPart(qualifiers);
    }
  }

  @Override
  public Class<?> getBeanClass() {
    return type;
  }

  @Override
  public Class<?> getObservedType() {
    return type;
  }

  @Override
  public Set<Annotation> getObservedQualifiers() {
    return observedQualifiers;
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

    final Map<String, Object> messageParts = new HashMap<String, Object>(10);
    messageParts.put(MessageParts.ToSubject.name(), getSubjectNameByType(event.getClass().getName()));
    messageParts.put(MessageParts.CommandType.name(), CDICommands.CDIEvent.name());
    messageParts.put(CDIProtocol.BeanType.name(), event.getClass().getName());
    messageParts.put(CDIProtocol.BeanReference.name(), event);

    if (!qualifierForWire.isEmpty()) {
      messageParts.put(CDIProtocol.Qualifiers.name(), qualifierForWire);
    }

    for (final String id : eventRoutingTable.getQueueIdsForRoute(event.getClass().getName(), qualifierForWire)) {
      bus.send(CommandMessage.createWithParts(new RoutingMap(messageParts, id)));
    }
  }
}
