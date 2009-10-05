package org.jboss.errai.bus.client;

public interface Message {
    public String getSubject();
    public Object getMessage();
}
