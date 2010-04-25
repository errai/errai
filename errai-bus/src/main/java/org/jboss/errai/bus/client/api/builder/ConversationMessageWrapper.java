package org.jboss.errai.bus.client.api.builder;

import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.framework.RoutingFlags;
import org.jboss.errai.bus.client.protocols.MessageParts;

import java.util.Map;


public class ConversationMessageWrapper implements Message {
    protected Message message;
    protected Message newMessage;

    public ConversationMessageWrapper(Message inReplyTo, Message newMessage) {
        this.message = inReplyTo;
        this.newMessage = newMessage;
    }

    public Message toSubject(String subject) {
        newMessage.toSubject(subject);
        return this;
    }

    public String getSubject() {
        return newMessage.getSubject();
    }

    public Message command(String type) {
        newMessage.command(type);
        return this;
    }

    public Message command(Enum type) {
        newMessage.command(type);
        return this;
    }

    public String getCommandType() {
        return newMessage.getCommandType();
    }

    public Message set(String part, Object value) {
        newMessage.set(part, value);
        return this;
    }

    public Message set(Enum part, Object value) {
        newMessage.set(part, value);
        return this;
    }

    public boolean hasPart(String part) {
        return newMessage.hasPart(part);
    }

    public boolean hasPart(Enum part) {
        return newMessage.hasPart(part);
    }

    public void remove(String part) {
        newMessage.remove(part);
    }

    public void remove(Enum part) {
        newMessage.remove(part);
    }

    public Message copy(String part, Message m) {
        newMessage.copy(part, m);
        return this;
    }

    public Message copy(Enum part, Message m) {
        newMessage.copy(part, m);
        return this;
    }

    public Message setParts(Map<String, Object> parts) {
        newMessage.setParts(parts);
        return this;
    }

    public Message addAllParts(Map<String, Object> parts) {
        newMessage.addAllParts(parts);
        return this;
    }

    public Map<String, Object> getParts() {
        return newMessage.getParts();
    }

    public void addResources(Map<String, ?> resources) {
        newMessage.addResources(resources);
    }

    public Message setResource(String key, Object res) {
        newMessage.setResource(key, res);
        return this;
    }

    public <T> T getResource(Class<T> type, String key) {
        return newMessage.getResource(type, key);
    }

    public boolean hasResource(String key) {
        return newMessage.hasResource(key);
    }

    public Message copyResource(String key, Message m) {
        newMessage.copyResource(key, m);
        return this;
    }

    public Message errorsCall(ErrorCallback callback) {
        newMessage.errorsCall(callback);
        return this;
    }

    public ErrorCallback getErrorCallback() {
        return newMessage.getErrorCallback();
    }

    public <T> T get(Class<T> type, String part) {
        return newMessage.get(type, part);
    }

    public <T> T get(Class<T> type, Enum part) {
        return newMessage.get(type, part);
    }

    public void setFlag(RoutingFlags flag) {
        newMessage.setFlag(flag);
    }

    public void unsetFlag(RoutingFlags flag) {
        newMessage.unsetFlag(flag);
    }

    public boolean isFlagSet(RoutingFlags flag) {
        return newMessage.isFlagSet(flag);
    }

    public void sendNowWith(MessageBus viaThis) {
        viaThis.send(this);
    }

    public void sendNowWith(RequestDispatcher viaThis) {
        viaThis.dispatch(this);
    }

    public Message getIncomingMessage() {
        return message;
    }

    public void commit() {
        if (!hasPart(MessageParts.ToSubject)) {
            if (message.hasPart(MessageParts.ReplyTo)) {
                toSubject(message.get(String.class, MessageParts.ReplyTo));
            } else {
                throw new RuntimeException("cannot have a conversation.  the incoming message does not specify a recipient ReplyTo subject and you have not specified one.");
            }
        }

        if (message.hasResource("Session")) {
            newMessage.copyResource("Session", message);
        } else {
            throw new RuntimeException("cannot have a conversation.  the incoming message has not session data associated with it.");
        }
    } 
}
