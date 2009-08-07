package org.jboss.workspace.server.bus;

public interface Message {
    public String getSubject();
    public String getMessage();
}
