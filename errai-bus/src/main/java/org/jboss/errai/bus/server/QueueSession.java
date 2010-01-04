package org.jboss.errai.bus.server;

public interface QueueSession {
    public String getSessionId();
    public boolean isValid();
    public boolean endSession();
    public void setAttribute(String attribute, Object value);
    public <T> T getAttribute(Class<T> type, String attribute);
    public boolean hasAttribute(String attribute);
    public void removeAttribute(String attribute);
}
