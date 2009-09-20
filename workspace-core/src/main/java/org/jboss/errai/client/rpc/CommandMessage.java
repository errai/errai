package org.jboss.errai.client.rpc;

import org.jboss.errai.client.rpc.protocols.MessageParts;

import java.util.HashMap;
import java.util.Map;

public class CommandMessage {
    protected Map<String, Object> parts = new HashMap<String, Object>();
    protected String encoded;

    public static CommandMessage create(String commandType) {
        return new CommandMessage(commandType);
    }

    public static CommandMessage create(Enum commandType) {
        return new CommandMessage(commandType);
    }

    public static CommandMessage create() {
        return new CommandMessage();
    }

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
        setCommandType(commandType);
    }

    public CommandMessage(Enum commandType) {
        setCommandType(commandType.name());
    }

    public CommandMessage(String subject, String commandType) {
        setSubject(subject).setCommandType(commandType);
    }


    public String getCommandType() {
        return String.valueOf(parts.get(MessageParts.CommandType.name()));
    }

    public String getSubject() {
        return String.valueOf(parts.get(MessageParts.Subject.name()));
    }

    public CommandMessage setSubject(String subject) {
        parts.put(MessageParts.Subject.name(), subject);
        return this;
    }

    public CommandMessage setCommandType(String type) {
        parts.put(MessageParts.CommandType.name(), type);
        return this;
    }

    public CommandMessage set(Enum part, Object value) {
        return set(part.name(), value);
    }

    public CommandMessage set(String part, Object value) {
        parts.put(part, value);
        encoded = null;
        return this;
    }

    public void remove(String part) {
        parts.remove(part);
        encoded = null;
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
