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
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeforeShutdown;
import javax.enterprise.inject.spi.ObserverMethod;

import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.cdi.server.CDIServerUtil;
import org.jboss.errai.cdi.server.TypeRegistry;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Observes CDI {@link javax.enterprise.inject.spi.BeforeShutdown} events and unsubcribes
 * previously registered subjects with the {@link org.jboss.errai.bus.client.framework.MessageBus}
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 15, 2010
 */
@ApplicationScoped
public class ShutdownEventObserver implements ObserverMethod {

  private static final Logger log = LoggerFactory.getLogger(ShutdownEventObserver.class);

  private TypeRegistry managedTypes;
  private MessageBus bus;
  private String uuid;

  public ShutdownEventObserver(TypeRegistry managedTypes, MessageBus bus, String uuid) {
    this.managedTypes = managedTypes;
    this.bus = bus;
    this.uuid = uuid;
  }

  public Class<?> getBeanClass() {
    return ShutdownEventObserver.class;
  }

  public Type getObservedType() {
    return BeforeShutdown.class;
  }

  public Set<Annotation> getObservedQualifiers() {
    Set<Annotation> qualifiers = new HashSet<Annotation>();
    return qualifiers;
  }

  public Reception getReception() {
    return Reception.ALWAYS;
  }

  public TransactionPhase getTransactionPhase() {
    return null;
  }

  public void notify(Object o) {
    log.info("Shutdown Errai-CDI context: " + uuid);

    // unsubscribe bean endpoints        
    for (AnnotatedType<?> svc : managedTypes.getServiceEndpoints()) {
      String subject = CDIServerUtil.resolveServiceName(svc.getJavaClass());
      log.debug("Unsubscribe: " + subject);
      bus.unsubscribeAll(subject);
    }

    for (Class<?> rpcIntf : managedTypes.getRpcEndpoints().keySet()) {
      String rpcSubjectName = rpcIntf.getName() + ":RPC";
      log.debug("Unsubscribe: " + rpcSubjectName);
      bus.unsubscribeAll(rpcSubjectName);
    }

    // unsubscribe event dispatcher endpoint
    bus.unsubscribeAll(CDI.DISPATCHER_SUBJECT);
  }
}
