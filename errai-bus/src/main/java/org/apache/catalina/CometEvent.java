package org.apache.catalina;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface CometEvent {
    public enum EventType {
        BEGIN, READ, END, ERROR
    }

    public enum EventSubType {
        TIMEOUT, CLIENT_DISCONNECT, IOEXCEPTION, WEBAPP_RELOAD, SERVER_SHUTDOWN, SESSION_END
    }

    public HttpServletRequest getHttpServletRequest();

    public HttpServletResponse getHttpServletResponse();

    public EventType getEventType();

    public EventSubType getEventSubType();

    public void close() throws IOException;

    public void setTimeout(int timeout)
            throws IOException, ServletException, UnsupportedOperationException;
}
