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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.context.Conversation;

/**
 * Acts as a bridge between an externally managed conversation handle
 * and the CDI container. It merely delegates to the default Conversation impl. but
 * retrieves the conversation id through a thread local.
 *
 * @see ContextManager#getThreadContextId()
 * @see Conversation#begin(String)
 *  
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 5, 2010
 */
public class ErraiConversation implements Conversation {

    private static final Logger log = LoggerFactory.getLogger(ErraiConversation.class);

    private Conversation delegate;
    private ContextManager contextManager;

    public ErraiConversation(Conversation delegate, ContextManager contextManager) {
        this.delegate = delegate;
        this.contextManager = contextManager;
    }

    public void begin() {
        String id = contextManager.getThreadContextId();
        if(id!=null)
        {
            log.debug("Begin conversation:  " + id);
            delegate.begin(id);
        }
        else
        {
            delegate.begin();
        }
    }

    public void begin(String id) {
        throw new IllegalArgumentException("An Errai managed conversation doesn't allow custom conversation ID's");
    }

    public void end() {
        log.debug("End conversation: " + delegate.getId());
        delegate.end();
    }

    public String getId() {
        return delegate.getId();
    }

    public long getTimeout() {
        return delegate.getTimeout();
    }

    public void setTimeout(long milliseconds) {
        delegate.setTimeout(milliseconds);
    }

    public boolean isTransient() {
        return delegate.isTransient();
    }
}
