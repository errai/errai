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
package org.jboss.errai.cdi.server.scopes;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.annotations.Command;
import org.jboss.errai.bus.server.annotations.Service;
import org.jboss.errai.cdi.server.ErraiConversation;

import javax.enterprise.context.ConversationScoped;
import javax.enterprise.context.NormalScope;
import javax.inject.Inject;
import java.io.Serializable;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 5, 2010
 */
@ConversationScoped
@Service("wizard")
public class TextBufferWizard implements Serializable {

    @Inject
    private transient ErraiConversation conversation;

    @Inject
    private transient MessageBus bus;

    private StringBuffer buffer = new StringBuffer();

    @Command("first")
    public void first(Message message)
    {
        conversation.begin();
        append(message);
    }

    @Command("append")
    public void append(Message message)
    {
        buffer.append(message.get(String.class, "word"));
        System.out.println("Current Buffer: " + buffer.toString());
    }

    @Command("last")
    public void last(Message message)
    {
        append(message);
        if(!conversation.isTransient())
            conversation.end();
        System.out.println("Flush Buffer");
        buffer = new StringBuffer();
    }
}
