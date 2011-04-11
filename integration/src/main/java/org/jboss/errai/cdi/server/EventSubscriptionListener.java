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

import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.SubscriptionEvent;
import org.jboss.errai.cdi.client.api.CDI;
import org.jboss.errai.cdi.server.events.EventObserverMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.spi.AfterBeanDiscovery;

/**
 * @author: Filip Rogaczewski
 */
@ApplicationScoped
public class EventSubscriptionListener implements SubscribeListener {

    private static final Logger log = LoggerFactory.getLogger(EventSubscriptionListener.class);

    private MessageBus bus;
    private AfterBeanDiscovery abd;
    private ContextManager mgr;

    public EventSubscriptionListener(AfterBeanDiscovery abd, MessageBus bus, ContextManager mgr) {
        this.abd = abd;
        this.bus = bus;
        this.mgr = mgr;
    }

    public void onSubscribe(SubscriptionEvent event) {
        try {
            if (event.isRemote() && event.getSubject().startsWith("cdi.event:")
                    && !event.getSubject().equals(CDI.DISPATCHER_SUBJECT) && event.getCount() == 1) {

                final String className = event.getSubject().substring("cdi.event:".length());
                final Class<?> type = this.getClass().getClassLoader().loadClass(className);

                abd.addObserverMethod(new EventObserverMethod(type, bus, mgr));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
