package org.jboss.errai.client.bus;

public interface Message {
    public String getSubject();
    public Object getMessage();
}
