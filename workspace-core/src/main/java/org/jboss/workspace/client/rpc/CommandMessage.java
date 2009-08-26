package org.jboss.workspace.client.rpc;

import org.jboss.workspace.client.rpc.protocols.MessageParts;

import java.util.HashMap;
import java.util.Map;

public class CommandMessage {
    private Map<String, Object> parts = new HashMap<String, Object>();

    public CommandMessage(Map<String, Object> parts) {
        this.parts = parts;
    }

    public CommandMessage(String commandType) {
        parts.put(MessageParts.CommandType.name(), commandType);
    }

    public CommandMessage(Enum commandType) {
        parts.put(MessageParts.CommandType.name(), commandType.name());
    }

    public String getCommandType() {
        return String.valueOf(parts.get(MessageParts.CommandType.name()));
    }

    public CommandMessage set(Enum part, Object value) {
        parts.put(part.name(), value);
        return this;
    }

    public <T> T get(Class<T> type, Enum part) {
        return (T) parts.get(part.name());
    }

    public Map<String, Object> getParts() {
        return parts;
    }
}
