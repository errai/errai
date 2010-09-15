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

import org.jboss.errai.cdi.server.EventDispatcher;
import org.jboss.errai.cdi.server.api.Outbound;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Reception;
import javax.enterprise.event.TransactionPhase;
import javax.enterprise.inject.spi.ObserverMethod;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

/**
 * Observes CDI events and delegates them to the {@link org.jboss.errai.cdi.server.EventDispatcher}
 * 
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 15, 2010
 */
@ApplicationScoped
public class OutboundEventObserver implements ObserverMethod {

    EventDispatcher dispatcher;

    public OutboundEventObserver(EventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    public Class<?> getBeanClass()
    {
        return OutboundEventObserver.class;
    }

    public Type getObservedType()
    {
        return Outbound.class;
    }

    public Set<Annotation> getObservedQualifiers()
    {
        Set<Annotation> qualifiers = new HashSet<Annotation>();
        return qualifiers;
    }

    public Reception getReception()
    {
        return Reception.ALWAYS;
    }

    public TransactionPhase getTransactionPhase()
    {
        return null;
    }

    public void notify(Object o)
    {
        if(null==dispatcher)
            throw new RuntimeException("EventDispatcher not initialized");
        dispatcher.sendMessage((Outbound)o);
    }
}
