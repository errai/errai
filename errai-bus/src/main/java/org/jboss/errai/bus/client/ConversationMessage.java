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

package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityParts;

public class ConversationMessage extends CommandMessage {
    public static ConversationMessage create(String commandType, CommandMessage inReplyTo) {
        return new ConversationMessage(commandType, inReplyTo);
    }

    public static ConversationMessage create(Enum commandType, CommandMessage inReplyTo) {
        return new ConversationMessage(commandType, inReplyTo);
    }

    public static ConversationMessage create(CommandMessage inReplyTo) {
        return new ConversationMessage(inReplyTo);
    }

    public ConversationMessage(CommandMessage inReplyTo) {
        if (inReplyTo.hasResource("Session")) {
            setResource("Session", inReplyTo.getResource("Session"));
        }
        if (inReplyTo.hasPart(MessageParts.ReplyTo)) {
            set(MessageParts.ToSubject, inReplyTo.get(String.class, MessageParts.ReplyTo));
        }

        if (!inReplyTo.hasResource("Session") && !inReplyTo.hasPart(MessageParts.ReplyTo)) {
            throw new RuntimeException("cannot have a conversation. there is no session data or ReplyTo field. Are you sure you referenced an incoming message?");
        }
    }


    public ConversationMessage(Enum commandType, CommandMessage inReplyTo) {
        this(inReplyTo);
        setCommandType(commandType.name());
    }

    public ConversationMessage(String commandType, CommandMessage inReplyTo) {
        this(inReplyTo);
        setCommandType(commandType);
    }


    public ConversationMessage toSubject(String subject) {
        parts.put(MessageParts.ToSubject.name(), subject);
        return this;
    }

    public ConversationMessage setCommandType(String type) {
        parts.put(MessageParts.CommandType.name(), type);
        return this;
    }

    public ConversationMessage set(Enum part, Object value) {
        return set(part.name(), value);
    }

    /**
     * Copy the same value from the specified message into this message.
     * @param part
     * @param message
     * @return
     */
    public ConversationMessage copy(Enum part, CommandMessage message) {
        set(part, message.get(Object.class, part));
        return this;
    }

    public ConversationMessage set(String part, Object value) {
        parts.put(part, value);
        return this;
    }
}
