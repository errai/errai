package org.jboss.errai.bus.client;

import java.util.Map;

public interface Message {
    public Message toSubject(String subject);
    public String getSubject();

    public Message command(String type);
    public Message command(Enum type);
    public String getCommandType();

    public Message set(String part, Object value);
    public Message set(Enum part, Object value);
    public boolean hasPart(String part);
    public boolean hasPart(Enum part);

    public void remove(String part);
    public void remove(Enum part);

    public Message copy(String part, Message m);
    public Message copy(Enum part, Message m);

    public Message setParts(Map<String,Object> parts);
    public Message addAllParts(Map<String, Object> parts);
    public Map<String, Object> getParts();

    public void addResources(Map<String, ?> resources);
    public Message setResource(String key, Object res);
    public Object getResource(String key);
    public boolean hasResource(String key);

    public Message copyResource(String key, Message m);

    public Message errorsCall(ErrorCallback callback);
    public ErrorCallback getErrorCallback();

    public <T> T get(Class<T> type, String part);
    public <T> T get(Class<T> type, Enum part);

    public void setFlag(RoutingFlags flag);
    public void unsetFlag(RoutingFlags flag);
    public boolean isFlagSet(RoutingFlags flag);

    public void sendNowWith(MessageBus viaThis);
    public void sendNowWith(RequestDispatcher viaThis);

}
