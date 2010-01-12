package org.jboss.servlet.http;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public interface HttpEvent {
    public enum EventType { BEGIN, END, ERROR, EVENT, READ, EOF, TIMEOUT, WRITE }

    public HttpServletRequest getHttpServletRequest();

    public HttpServletResponse getHttpServletResponse();

    public EventType getType();

    public void close() throws IOException;

    public void setTimeout(int timeout)
        throws IOException, ServletException, UnsupportedOperationException;
}
