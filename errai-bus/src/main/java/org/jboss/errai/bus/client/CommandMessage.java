package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.types.TypeHandlerFactory;

import java.util.HashMap;
import java.util.Map;

public class CommandMessage {
    protected Map<String, Object> parts = new HashMap<String, Object>();

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
        Object value = parts.get(part.name());
        try {
            return (T) value;
        }
        catch (ClassCastException e) {
            // try converstion.  Unfortunately since the GWT Client Emulation library
            // does not implement Class.isAssignableFrom(), doing this via a CCE is currently
            // the most efficient way to implement this.
            return (T) TypeHandlerFactory.convert(value.getClass(), type, value);
        }


    }

    @SuppressWarnings({"UnusedDeclaration"})
    public <T> T get(Class<T> type, String part) {
        //noinspection unchecked
        Object value = parts.get(part);
        try {
            return (T) value;
        }
        catch (ClassCastException e) {
            // try converstion.  Unfortunately since the GWT Client Emulation library
            // does not implement Class.isAssignableFrom(), doing this via a CCE is currently
            // the most efficient way to implement this.
            return (T) TypeHandlerFactory.convert(value.getClass(), type, value);
        }

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

    public void sendNowWith(MessageBus viaThis) {
        viaThis.send(this);
    }

}
