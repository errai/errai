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
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.enterprise.inject.spi.BeanManager;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.cdi.client.CDICommands;
import org.jboss.errai.cdi.client.CDIProtocol;
import org.jboss.weld.manager.BeanManagerImpl;

/**
 * Acts as a bridge between Errai Bus and the CDI event system.<br/>
 * Includes marshalling/unmarshalling of event types.
 */
public class EventDispatcher implements MessageCallback {
    private BeanManager beanManager;
    private MessageBus bus;
    private ContextManager ctxMgr;

    private Map<Class<?>, Class<?>> conversationalEvents = new HashMap<Class<?>, Class<?>>();
    private Set<Class<?>> conversationalServices = new HashSet<Class<?>>();

    private Set<String> observedEvents;
    private Map<String, Annotation> allQualifiers;
    
    public EventDispatcher(BeanManager beanManager, MessageBus bus, ContextManager ctxMgr, Set<String> observedEvents,
    		Map<String, Annotation> qualifiers) {
        this.beanManager = beanManager;
        this.bus = bus;
        this.ctxMgr = ctxMgr;
        this.observedEvents = observedEvents;
        this.allQualifiers = qualifiers;
    }

    // Invoked by Errai
    public void callback(final Message message) {
        try {
            /**
             * If the message didn't not come from a remote, we don't handle it.
             */
            if (!message.isFlagSet(RoutingFlags.FromRemote)) return;

            switch (CDICommands.valueOf(message.getCommandType())) {
                case CDIEvent:
                    String type = message.get(String.class, CDIProtocol.TYPE);
                    final Class clazz = Thread.currentThread().getContextClassLoader().loadClass(type);
                    final Object o = message.get(clazz, CDIProtocol.OBJECT_REF);
                    try {
                        ctxMgr.activateRequestContext();

                        if (conversationalServices.contains(clazz)) {
                            ctxMgr.getRequestContextStore().put(MessageParts.SessionID.name(),
                                    Util.getSessionId(message));
                        }
                        
                        Set<String> qualifierNames = message.get(Set.class, CDIProtocol.QUALIFIERS);
                        List<Annotation> qualifiers = null;
                        if(qualifierNames!=null) {
	                        for(String qualifierName : qualifierNames) {
                        		if(qualifiers==null) {
                        			qualifiers = new ArrayList<Annotation>();
                        		}
                        		Annotation qualifier=allQualifiers.get(qualifierName);
                        		if(qualifier!=null) {
                        			qualifiers.add(qualifier);
                        		}
	                        }
                        }
                        
                        if(qualifiers!=null) {
                        	beanManager.fireEvent(o, qualifiers.toArray(new Annotation[qualifiers.size()]));	
                        } else {
                        	beanManager.fireEvent(o);
                        }
                        
                        if (conversationalEvents.containsKey(clazz)) {

                            final Class outType = conversationalEvents.get(clazz);
                            final String outTypeStr = outType.getName();
                            final String sessionId = Util.getSessionId(message);

                            /**
                             * TODO: This effectively hard-codes us to Weld. But the CDI specification has no way
                             *       of calling dynamically qualified types from BeanManager.
                             */
                            ((BeanManagerImpl) beanManager)
                                    .fireEvent(new ParameterizedType() {
                                        public Type[] getActualTypeArguments() {
                                            return new Type[]{clazz, outType};
                                        }

                                        public Type getRawType() {
                                            return ConversationalEvent.class;
                                        }

                                        public Type getOwnerType() {
                                            return ConversationalEvent.class;
                                        }
                                    }, new ConversationalEvent<Object, Object>() {
                                        public Object getEvent() {
                                            return o;
                                        }

                                        public void fire(Object o) {
                                            MessageBuilder.createMessage()
                                                    .toSubject("cdi.event:" + outTypeStr)
                                                    .command(CDICommands.CDIEvent)
                                                    .with(MessageParts.SessionID, sessionId)
                                                    .with(CDIProtocol.TYPE, outTypeStr)
                                                    .with(CDIProtocol.OBJECT_REF, o)
                                                    .noErrorHandling().sendNowWith(bus);
                                        }
                                    });
                        }

                    } finally {
                        ctxMgr.deactivateRequestContext();
                    }

                    break;

                case AttachRemote:
                    MessageBuilder.createConversation(message)
                            .toSubject("cdi.event:ClientDispatcher")
                            .command(BusCommands.RemoteSubscribe)
                            .with(MessageParts.Value, observedEvents.toArray(new String[observedEvents.size()]))
                            .done().reply();

                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unknown command type " + message.getCommandType());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to dispatch CDI Event", e);
        }
    }

    public void registerConversationEvent(Class<?> clientEvent, Class<?> serverEvent) {
        conversationalEvents.put(clientEvent, serverEvent);
    }

    public void registerConversationalService(Class<?> conversational) {
        conversationalServices.add(conversational);
    }
}
