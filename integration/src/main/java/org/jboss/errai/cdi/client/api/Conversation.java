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
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.api.builder.MessageBuildParms;
import org.jboss.errai.bus.client.api.builder.MessageBuildSendableWithReply;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Oct 5, 2010
 */
public class Conversation {

    private String id;
    private String subject;

    private boolean ended = false;

    public Conversation(String id, String subject) {
        this.id = id;
        this.subject = subject;
    }

    public MessageBuildParms<MessageBuildSendableWithReply>  createMessage()
    {
        assertEnded();
        MessageBuildParms<MessageBuildSendableWithReply> parms = MessageBuilder.createMessage()
                .toSubject(subject)
                .signalling()
                .with("conversationId", id);                                                             

        return parms;
    }

    public MessageBuildParms<MessageBuildSendableWithReply>  createMessage(String command)
    {
        assertEnded();
        MessageBuildParms<MessageBuildSendableWithReply> parms = MessageBuilder.createMessage()
                .toSubject(subject)
                .command(command)
                .with("conversationId", id);

        return parms;
    }

    public MessageBuildParms<MessageBuildSendableWithReply>  createMessage(Enum command)
    {
        assertEnded();
        MessageBuildParms<MessageBuildSendableWithReply> parms = MessageBuilder.createMessage()
                .toSubject(subject)
                .command(command)
                .with("conversationId", id);

        return parms;
    }

    public String getId() {
        return id;
    }

    /**
     * Explicitly end a conversation
     */
    public void end()
    {
        assertEnded();
        
        MessageBuilder.createMessage()
                .toSubject("cdi.conversation:Manager,conversation="+id)
                .command("end")
                .with("conversationId", id)
                .done().sendNowWith(ErraiBus.get());

        ended = true;
    }

    private void assertEnded()
    {
        throw new IllegalStateException("Converation already ended: "+ id);       
    }
}
