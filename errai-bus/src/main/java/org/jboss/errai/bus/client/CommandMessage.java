package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.common.client.types.TypeHandlerFactory;

import java.util.HashMap;
import java.util.Map;

public class CommandMessage {
    protected Map<String, Object> parts = new HashMap<String, Object>();
    protected Map<String, Object> resources;

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

    public CommandMessage(String commandType) {
        setCommandType(commandType);
    }

    public CommandMessage(Enum commandType) {
        setCommandType(commandType.name());
    }

    public CommandMessage(String subject, String commandType) {
        toSubject(subject).setCommandType(commandType);
    }

    public String getCommandType() {
        return (String) parts.get(MessageParts.CommandType.name());
    }

    public String getSubject() {
        return String.valueOf(parts.get(MessageParts.ToSubject.name()));
    }

    public CommandMessage toSubject(String subject) {
        parts.put(MessageParts.ToSubject.name(), subject);
        return this;
    }

    public CommandMessage setCommandType(Enum type) {
        parts.put(MessageParts.CommandType.name(), type.name());
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
        return this;
    }

    /**
     * Copy the same value from the specified message into this message.
     *
     * @param part    -
     * @param message -
     * @return -
     */
    public CommandMessage copy(Enum part, CommandMessage message) {
        set(part, message.get(Object.class, part));
        return this;
    }

    public CommandMessage copy(String part, CommandMessage message) {
        set(part, message.get(Object.class, part));
        return this;
    }


    public void remove(String part) {
        parts.remove(part);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public <T> T get(Class<T> type, Enum part) {
        //noinspection unchecked
        Object value = parts.get(part.toString());
        return value == null ? null : (T) TypeHandlerFactory.convert(value.getClass(), type, value);
    }

    @SuppressWarnings({"UnusedDeclaration"})
    public <T> T get(Class<T> type, String part) {
        //noinspection unchecked
        Object value = parts.get(part);
        return value == null ? null : (T) TypeHandlerFactory.convert(value.getClass(), type, value);
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

    public CommandMessage setParts(Map<String, Object> parts) {
        this.parts = parts;
        return this;
    }

    public CommandMessage setResource(String key, Object res) {
        if (this.resources == null) this.resources = new HashMap<String, Object>();
        this.resources.put(key, res);
        return this;
    }

    public Object getResource(String key) {
        return this.resources == null ? null : this.resources.get(key);
    }

    public CommandMessage copyResource(String key, CommandMessage copyFrom) {
        if (!copyFrom.hasResource(key)) {
            throw new RuntimeException("Cannot copy resource '" + key + "': no such resource.");
        }
        setResource(key, copyFrom.getResource(key));
        return this;
    }

    public boolean hasResource(String key) {
        return this.resources != null && this.resources.containsKey(key);
    }

    public void addResources(Map<String, ?> resources) {
        if (this.resources == null) this.resources = new HashMap<String, Object>(resources);
        else {
            this.resources.putAll(resources);
        }
    }

    public void sendNowWith(MessageBus viaThis) {
        viaThis.send(this);
    }

    @Override
    public String toString() {
        return "CommandMessage(toSubject=" + getSubject() + ";CommandType=" + getCommandType() + ")";
    }
}
