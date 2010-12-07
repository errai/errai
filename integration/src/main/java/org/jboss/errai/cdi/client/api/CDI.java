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
package org.jboss.errai.cdi.client.api;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.MessageInterceptor;
import org.jboss.errai.cdi.client.CDICommands;
import org.jboss.errai.cdi.client.CDIProtocol;
import org.jboss.errai.cdi.client.EventHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * CDI client interface.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Apr 9, 2010
 */
public class CDI {
    static private final MessageBus bus = ErraiBus.get();

    static private Map<String, Conversation> activeConversations =
            new HashMap<String, Conversation>();

    public static MessageInterceptor CONVERSATTION_INTERCEPTOR = new ConversationInterceptor();

    public static void handleEvent(final Class<?> type, final EventHandler handler) {
        bus.subscribe("cdi.event:" + type.getName(), // by convention
                new MessageCallback() {
                    public void callback(Message message) {
                        Object response = message.get(type, CDIProtocol.OBJECT_REF);
                        handler.handleEvent(response);
                    }
                }
        );
    }

    public static void fireEvent(final Object payload) {
        MessageBuilder.createMessage()
                .toSubject("cdi.event:Dispatcher")
                .command(CDICommands.CDI_EVENT)
                .with(CDIProtocol.TYPE, payload.getClass().getName())
                .with(CDIProtocol.OBJECT_REF, payload)
                .noErrorHandling()
                .sendNowWith(bus);
    }

    public static String generateId()
    {
        return String.valueOf(com.google.gwt.user.client.Random.nextInt(1000))
                + "-" + (System.currentTimeMillis() % 1000);
    }

    public static Conversation createConversation(String withSubject)
    {
        Conversation conversation = new Conversation(generateId(), withSubject);        
        return conversation;
    }

    public static Map<String, Conversation> getActiveConversations() {
        return activeConversations;
    }

    /**
     * Decorates a message with the conversation id if required
     */
    static class ConversationInterceptor implements MessageInterceptor
    {
        public boolean processOutbound(Message message) {

            // skip if none active
            if(getActiveConversations().isEmpty()) return true;

            // internal channel, don't decorate message
            if(message.hasPart("cdi.internal")) return true;

            // find a conversation handle exist for this subject
            Set<String> activeConversations = getActiveConversations().keySet();
            Conversation conversationHandle = null;
            for(String id : activeConversations)
            {
                Conversation c = getActiveConversations().get(id);
                if(c.getSubject().equals(message.getSubject()))
                {
                    conversationHandle = c;
                    break;
                }
            }

            // if there is a matching active conversation for a particular subject
            // we attach the conversation id
            if(conversationHandle!=null && conversationHandle.isActive())
            {
                Map<String, Object> parts = new HashMap<String,Object>(getActiveConversations().size());
                parts.put("cdi.conversation.id", conversationHandle.getId());
                message.addAllParts(parts);
            }
            
            return true;
        }

        public boolean processInbound(Message message) {
            return true;
        }
    }
}
