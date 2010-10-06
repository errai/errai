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
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.weld.context.bound.BoundConversationContext;
import org.jboss.weld.context.bound.BoundRequestContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.BeanManager;
import java.util.HashMap;
import java.util.Map;
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

        this.bus = bus;
        this.uuid = uuid;
        
        this.requestContext = (BoundRequestContext)
                Util.lookupCallbackBean(beanManager, BoundRequestContext.class);

        this.conversationContext= (BoundConversationContext)
                Util.lookupCallbackBean(beanManager, BoundConversationContext.class);

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
        threadContextId.set(conversationId); // null value reset demote the conversation
        conversationContext.activate(threadContextId.get());

        // expose the conversation context to the client
        // TODO: wire CDI callbacks when conversation ends
        String subject = "cdi.conversation:Manager,conversation=" + conversationId;
        if(conversationId!=null && !bus.isSubscribed(subject))
        {
            bus.subscribe(subject,
                    new MessageCallback()
                    {
                        public void callback(final Message message) {
                                if("end".equals(message.getCommandType()))
                                try {
                                    activateConversationContext(message);
                                    conversationContext.getCurrentConversation().end();                                    
                                    deactivateConversationContext(message);

                                    // TODO: properly cleanup
                                    
                                } catch (Exception e) {
                                    log.error("Failed to end conversation", e);
                                }
                        }
                    });
        }
    }
    
    public void deactivateConversationContext(Message message)
    {
        String sessionId = Util.getSessionId(message);

        if(conversationContextStore.get(sessionId)!=null) {            
            conversationContext.deactivate();
            conversationContext.dissociate(conversationContextStore.get(sessionId));
        }
    }
    
    public String getThreadContextId() {        
        return threadContextId.get();
    }
}
