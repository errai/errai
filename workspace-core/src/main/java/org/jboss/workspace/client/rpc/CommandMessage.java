package org.jboss.workspace.client.rpc;

import org.jboss.workspace.client.rpc.protocols.MessageParts;

import java.util.HashMap;
import java.util.Map;

public class CommandMessage {
    private Map<String, Object> parts = new HashMap<String, Object>();
    private String encoded;

    public CommandMessage() {
    }

    public CommandMessage(Map<String, Object> parts) {
        this.parts = parts;
    }

    public CommandMessage(Map<String, Object> parts, String encoded) {
        this.parts = parts;
        this.encoded = encoded;
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
        return set(part.name(), value);
    }

    public CommandMessage set(String part, Object value) {
        parts.put(part, value);
        encoded = null;
        return this;
    }

    public <T> T get(Class<T> type, Enum part) {
        return (T) parts.get(part.name());
    }

    public <T> T get(Class<T> type, String part) {
        return (T) parts.get(part);
    }

    public boolean hasPart(Enum part) {
        return hasPart(part.name());
    }

    public boolean hasPart(String part) {
        return parts.containsKey(part);
    }

    public Map<String, Object> getParts() {
        return parts;
    }

    public void setParts(Map<String, Object> parts) {
        this.parts = parts;
    }

    public boolean hasCachedEncoding() {
        return encoded != null;
    }

    public String getEncoded() {
        return encoded;
    }


}
