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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * An implementation of the the CDI SPI {@code ObserverMethod} interface which is used to intercept events within the 
 * CDI container. The purpose of this implementation is to observe an event which is exposed to the bus and
 * transmit the event to all clients listening to this event.</p>
 * 
 * For the "conversational" version of this, see {@link ConversationalEventObserverMethod}.
 * 
 * @author Mike Brock
 */
public class EventObserverMethod implements ObserverMethod {

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
  protected final List<String> qualifierForWire;

  /**
   * An instance of the MessageBus.
   */
  protected final MessageBus bus;

  /**
   * The pre-calculated subject to be used to transmit the event remotely.
   */
  protected final String subject;

  public EventObserverMethod(Class<?> type, MessageBus bus, Annotation... qualifiers) {
    this.type = type;
    this.bus = bus;

    if (qualifiers == null || qualifiers.length == 0) {
      this.observedQualifiers = Collections.emptySet();
      this.qualifierForWire = Collections.emptyList();
    }
    else {
      this.observedQualifiers = Collections.unmodifiableSet(new HashSet<Annotation>(Arrays.asList(qualifiers)));
      this.qualifierForWire = CDI.getQualifiersPart(qualifiers);
    }

    this.subject = CDI.getSubjectNameByType(type);
  }

  public Class<?> getBeanClass() {
    return type;
  }

  public Class<?> getObservedType() {
    return type;
  }

  public Set<Annotation> getObservedQualifiers() {
    return observedQualifiers;
  }

  public Reception getReception() {
    return Reception.ALWAYS;
  }

  public TransactionPhase getTransactionPhase() {
    return null;
  }

  public void notify(Object event) {
    if (!type.equals(event.getClass()) || EventConversationContext.isEventObjectInContext(event)) return;

    if (!qualifierForWire.isEmpty()) {
      MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
              .with(CDIProtocol.BeanType, type.getName()).with(CDIProtocol.BeanReference, event)
              .with(CDIProtocol.Qualifiers, qualifierForWire).noErrorHandling().sendNowWith(bus);
    }
    else {
      MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
              .with(CDIProtocol.BeanType, type.getName()).with(CDIProtocol.BeanReference, event).noErrorHandling()
              .sendNowWith(bus);
    }
  }
}
