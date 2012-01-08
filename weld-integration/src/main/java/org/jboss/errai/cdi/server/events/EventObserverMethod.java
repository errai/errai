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
import java.util.*;

/**
 * @author Filip Rogaczewski
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class EventObserverMethod implements ObserverMethod {

  protected final Class<?> type;
  protected final Set<Annotation> observedQualifiers;
  protected final Annotation[] qualifiers;
  protected final List<String> qualifierForWire;
  protected final MessageBus bus;
  protected final String subject;
  protected final String qualifiersString;

  public EventObserverMethod(Class<?> type, MessageBus bus, Annotation... qualifiers) {
    this.type = type;
    this.bus = bus;
    this.qualifiers = qualifiers == null ? new Annotation[0] : qualifiers;
    this.observedQualifiers = (this.qualifiers.length == 0) ? Collections.<Annotation>emptySet()
            : Collections.unmodifiableSet(new HashSet<Annotation>(Arrays.asList(qualifiers)));

    // don't be clever and make this an unmodifiableList. Errai Marshalling can't currently deal with it.
    this.qualifierForWire = (this.qualifiers.length == 0) ? Collections.<String>emptyList()
            : CDI.getQualifiersPart(qualifiers);

    this.qualifiersString = Arrays.toString(qualifiers);

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
