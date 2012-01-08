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

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Filip Rogaczewski
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class EventObserverMethod implements ObserverMethod {

  protected final Class<?> type;
  protected Annotation[] qualifiers;
  protected Set<String> qualifierForWire;
  protected MessageBus bus;
  protected String subject;

  public EventObserverMethod(Class<?> type, MessageBus bus, Annotation... qualifiers) {
    this.type = type;
    this.bus = bus;
    this.qualifiers = qualifiers;
    this.qualifierForWire = CDI.getQualifiersPart(qualifiers);
    this.subject = CDI.getSubjectNameByType(type);
  }

  public Class<?> getBeanClass() {
    return EventObserverMethod.class;
  }

  public Class<?> getObservedType() {
    return type;
  }

  public Set<Annotation> getObservedQualifiers() {
    if (qualifiers != null) {
      return new HashSet<Annotation>(Arrays.asList(qualifiers));
    }
    else {
      return new HashSet<Annotation>();
    }
  }

  public Reception getReception() {
    return Reception.ALWAYS;
  }

  public TransactionPhase getTransactionPhase() {
    return null;
  }

  public void notify(Object event) {
    EventConversationContext.Context ctx = EventConversationContext.get();
    if (ctx != null) {
      if (ctx.alreadyHandled(event)) return;
      ctx.record(event);
    }

    if (qualifierForWire != null && !qualifierForWire.isEmpty()) {
      MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
              .with(CDIProtocol.TYPE, type.getName()).with(CDIProtocol.OBJECT_REF, event)
              .with(CDIProtocol.QUALIFIERS, qualifierForWire).noErrorHandling().sendNowWith(bus);
    }
    else {
      MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
              .with(CDIProtocol.TYPE, type.getName()).with(CDIProtocol.OBJECT_REF, event).noErrorHandling()
              .sendNowWith(bus);
    }
  }
}
