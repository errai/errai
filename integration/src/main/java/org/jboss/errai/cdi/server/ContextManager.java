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

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.weld.context.ManagedConversation;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.BeanManager;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 28, 2010
 */
public class ContextManager {

    private static final Logger log = LoggerFactory.getLogger(ContextManager.class);
    
    private BoundRequestContext requestContext;
    private BoundConversationContext conversationContext;

    private ThreadLocal<Map<String, Object>> requestContextStore =
            new ThreadLocal<Map<String, Object>>();

    private Map<String, ConversationContext> conversationContextStore =
            new ConcurrentHashMap<String, ConversationContext>();

    private String uuid;

    private ThreadLocal<String> threadContextId = new ThreadLocal<String>();

    private MessageBus bus;
    private static final String CDI_CONVERSATION_MANAGER = "cdi.conversation:Manager";

    public ContextManager(String uuid, BeanManager beanManager, MessageBus bus) {

        this.requestContext = (BoundRequestContext)
                Util.lookupCallbackBean(beanManager, BoundRequestContext.class);

        this.conversationContext= (BoundConversationContext)
                Util.lookupCallbackBean(beanManager, BoundConversationContext.class);

        this.bus = bus;
    }

    public void activateRequestContext()
    {
        requestContextStore.set(new HashMap<String, Object>());
        requestContext.associate(requestContextStore.get());
        requestContext.activate();
    }

    public void deactivateRequestContext()
    {
        requestContext.invalidate();
        requestContext.deactivate();
        requestContext.dissociate(requestContextStore.get());
    }

    public void activateConversationContext(Message message)
    {
        String sessionId = Util.getSessionId(message);
        
        if(null==conversationContextStore.get(sessionId))
            conversationContextStore.put(sessionId, new ConversationContext());
        
        conversationContext.associate(conversationContextStore.get(sessionId));

        // if the client does not provide a conversation id
        // we fall back to transient conversations (id==null)
        String conversationId = message.get(String.class, "conversationId");
        threadContextId.set(conversationId);
        conversationContext.activate(threadContextId.get()); 
    }

    private String generateThreadContextId(String sessionId)
    {
        return sessionId + "__" + UUID.randomUUID().toString();   
    }

    public void deactivateConversationContext(Message message)
    {
        String sessionId = Util.getSessionId(message);

        if(conversationContextStore.get(sessionId)!=null) {
            ManagedConversation managedConversation = conversationContext.getCurrentConversation();

            // In case a conversation has been created, we need to pass the reference to the client
            if(!managedConversation.isTransient())
            {
                MessageBuilder.createConversation(message)
                        .toSubject(CDI_CONVERSATION_MANAGER)
                        .with("conversationId", threadContextId.get())
                        .with(MessageParts.PriorityProcessing, "1")
                        .done().sendNowWith(bus);                        
            }

            conversationContext.deactivate();
            conversationContext.dissociate(conversationContextStore.get(sessionId));
        }
    }
    
    public String getThreadContextId() {        
        return threadContextId.get();
    }
}
