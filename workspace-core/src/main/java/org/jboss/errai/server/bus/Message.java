package org.jboss.errai.server.bus;

public interface Message {
    public String getSubject();
    public Object getMessage();
}
