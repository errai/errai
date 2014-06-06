package org.jboss.errai.bus.server.websocket.jsr356.filter;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * Registered implementations of this filter in web.xml will be invoked for each
 * inbound message on the WebSocket channel.
 * 
 * @author Michel Werren
 */
public interface WebSocketFilter {
  public void doFilter(Session websocketSession, HttpSession httpSession, String message);
}
