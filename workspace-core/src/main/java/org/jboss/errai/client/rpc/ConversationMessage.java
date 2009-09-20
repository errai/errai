package org.jboss.errai.client.rpc;

import org.jboss.errai.client.rpc.protocols.MessageParts;
import org.jboss.errai.client.rpc.protocols.SecurityParts;

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
        if (inReplyTo.hasPart(SecurityParts.SessionData)) {
            set(SecurityParts.SessionData, inReplyTo.get(Object.class, SecurityParts.SessionData));
        }
        else if (inReplyTo.hasPart(MessageParts.ReplyTo)) {
            set(MessageParts.ToSubject, inReplyTo.get(String.class, MessageParts.ReplyTo));
        }
        else {
            throw new RuntimeException("cannot have a conversation. there is no session data or ReplyTo field");
        }
    }


    public ConversationMessage(Enum commandType, CommandMessage inReplyTo) {
        this(inReplyTo);
        setCommandType(commandType.name());
    }

    public ConversationMessage(String commandType, CommandMessage inReplyTo) {
        this(inReplyTo);
        set(SecurityParts.SessionData, inReplyTo.get(Object.class, SecurityParts.SessionData));
        setCommandType(commandType);
    }


    public ConversationMessage setSubject(String subject) {
        parts.put(MessageParts.Subject.name(), subject);
        return this;
    }

    public ConversationMessage setCommandType(String type) {
        parts.put(MessageParts.CommandType.name(), type);
        return this;
    }

    public ConversationMessage set(Enum part, Object value) {
        return set(part.name(), value);
    }

    public ConversationMessage set(String part, Object value) {
        parts.put(part, value);
        encoded = null;
        return this;
    }
}
