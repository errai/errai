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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.cdi.server.ContextManager;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.enterprise.client.cdi.CDICommands;
import org.jboss.errai.enterprise.client.cdi.CDIProtocol;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * @author Filip Rogaczewski
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class EventObserverMethod implements ObserverMethod {

  private Class<?> type;
  private Annotation[] qualifiers;
  private MessageBus bus;
  private String subject;
  private ContextManager mgr;

  public EventObserverMethod(Class<?> type, MessageBus bus, ContextManager mgr, Annotation... qualifiers) {
    this.type = type;
    this.bus = bus;
    this.mgr = mgr;
    this.qualifiers = qualifiers;
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
    } else {
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
    if (!type.isInstance(event))
      return;

    Map<String, Object> ctx = mgr.getRequestContextStore();
    Set<String> qualifiersPart = CDI.getQualifiersPart(qualifiers);
    Map store = mgr.getRequestContextStore();

    if (store != null && event == store.get(CDIProtocol.OBJECT_REF.name())) {
      return;
    }

    if (ctx != null && ctx.containsKey(MessageParts.SessionID.name())) {
      if (qualifiersPart != null && !qualifiersPart.isEmpty()) {
        MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
            .with(MessageParts.SessionID.name(), ctx.get(MessageParts.SessionID.name()))
            .with(CDIProtocol.TYPE, type.getName()).with(CDIProtocol.QUALIFIERS, qualifiersPart)
            .with(CDIProtocol.OBJECT_REF, event)
                .flag(RoutingFlags.NonGlobalRouting).noErrorHandling().sendNowWith(bus);
      } else {
        MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
            .with(MessageParts.SessionID.name(), ctx.get(MessageParts.SessionID.name()))
            .with(CDIProtocol.TYPE, type.getName()).with(CDIProtocol.OBJECT_REF, event)
                .flag(RoutingFlags.NonGlobalRouting).noErrorHandling()
            .sendNowWith(bus);
      }
    } else {
      if (qualifiersPart != null && !qualifiersPart.isEmpty()) {
        MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
            .with(CDIProtocol.TYPE, type.getName()).with(CDIProtocol.OBJECT_REF, event)
            .with(CDIProtocol.QUALIFIERS, qualifiersPart).noErrorHandling().sendNowWith(bus);
      } else {
        MessageBuilder.createMessage().toSubject(subject).command(CDICommands.CDIEvent)
            .with(CDIProtocol.TYPE, type.getName()).with(CDIProtocol.OBJECT_REF, event).noErrorHandling()
            .sendNowWith(bus);
      }
    }
  }
}
