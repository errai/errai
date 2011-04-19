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
package org.jboss.errai.cdi.server;

import java.lang.annotation.Annotation;
import java.util.Map;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.AfterBeanDiscovery;

import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.cdi.server.events.EventObserverMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Filip Rogaczewski
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@ApplicationScoped
public class EventSubscriptionListener implements SubscribeListener {

    private static final Logger log = LoggerFactory.getLogger(EventSubscriptionListener.class);

    private MessageBus bus;
    private AfterBeanDiscovery abd;
    private ContextManager mgr;
    private Map<String, Annotation[]> observedEventsSet;

    public EventSubscriptionListener(AfterBeanDiscovery abd, MessageBus bus, ContextManager mgr, Map<String, Annotation[]> observedEvents) {
        this.abd = abd;
        this.bus = bus;
        this.mgr = mgr;
        this.observedEventsSet = observedEvents;
    }

    public void onSubscribe(SubscriptionEvent event) {
        if (event.isLocalOnly() || !event.isRemote() || !event.getSubject().startsWith("cdi.event:")) return;

        String name = event.getSubject().substring("cdi.event:".length());
        try {
            if (observedEventsSet.containsKey(name) && event.getCount() == 1) {
                final Class<?> type = this.getClass().getClassLoader().loadClass(name.split("@")[0]);
                abd.addObserverMethod(new EventObserverMethod(type, bus, mgr, observedEventsSet.get(name)));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
