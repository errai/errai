package org.jboss.workspace.client.rpc;

import org.jboss.workspace.client.rpc.protocols.SecurityParts;
import org.jboss.workspace.client.rpc.protocols.MessageParts;

public class ConversationMessage extends CommandMessage {
    public static ConversationMessage create(String commandType, CommandMessage inReplyTo) {
        return new ConversationMessage(commandType, inReplyTo);
    }

    public static ConversationMessage create(Enum commandType, CommandMessage inReplyTo) {
        return new ConversationMessage(commandType, inReplyTo);
    }

    public ConversationMessage(Enum commandType, CommandMessage inReplyTo) {
        if (!inReplyTo.hasPart(SecurityParts.SessionData)) {
            System.out.println("ERROR");
            throw new RuntimeException("cannot have a conversation. there is no session data.");
        }
        set(SecurityParts.SessionData, inReplyTo.get(Object.class, SecurityParts.SessionData));
        setCommandType(commandType.name());
    }

    public ConversationMessage(String commandType, CommandMessage inReplyTo) {
        if (!inReplyTo.hasPart(SecurityParts.SessionData)) {
            throw new RuntimeException("cannot have a conversation. there is no session data.");
        }
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
