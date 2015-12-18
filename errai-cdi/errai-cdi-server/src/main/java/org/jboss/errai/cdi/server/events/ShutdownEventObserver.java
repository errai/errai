/*
 * Copyright (C) 2009 Red Hat, Inc. and/or its affiliates.
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.server.util.ServiceParser;
import org.jboss.errai.cdi.server.TypeRegistry;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observes CDI {@link javax.enterprise.inject.spi.BeforeShutdown} events and unsubcribes
 * previously registered subjects with the {@link org.jboss.errai.bus.client.api.messaging.MessageBus}
 *
 * @author Heiko Braun <hbraun@redhat.com>
 */
@SuppressWarnings("rawtypes")
public class ShutdownEventObserver implements ObserverMethod {
  private static final Logger log = LoggerFactory.getLogger(ShutdownEventObserver.class);

  private TypeRegistry managedTypes;
  private MessageBus bus;

  public ShutdownEventObserver(TypeRegistry managedTypes, MessageBus bus) {
    this.managedTypes = managedTypes;
    this.bus = bus;
  }

  @Override
  public Class<?> getBeanClass() {
    return ShutdownEventObserver.class;
  }

  @Override
  public Type getObservedType() {
    return BeforeShutdown.class;
  }

  @Override
  public Set<Annotation> getObservedQualifiers() {
    return Collections.emptySet();
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
  public void notify(Object o) {
    log.info("Shutting down CDI-to-ErraiBus event bridge");
    // unsubscribe bean endpoints
    for (Class<?> delegateClass : managedTypes.getDelegateClasses()) {
      for (ServiceParser svcParser : managedTypes.getDelegateServices(delegateClass)) {
        final String subject = svcParser.getServiceName();
        log.debug("unsubscribe: " + subject);
        bus.unsubscribeAll(subject);        
      }
    }

    for (Class<?> rpcIntf : managedTypes.getRemoteInterfaces()) {
      final String rpcSubjectName = rpcIntf.getName() + ":RPC";
      log.debug("unsubscribe: " + rpcSubjectName);
      bus.unsubscribeAll(rpcSubjectName);
    }

    // unsubscribe event dispatcher endpoint
    bus.unsubscribeAll(CDI.SERVER_DISPATCHER_SUBJECT);
  }
}
