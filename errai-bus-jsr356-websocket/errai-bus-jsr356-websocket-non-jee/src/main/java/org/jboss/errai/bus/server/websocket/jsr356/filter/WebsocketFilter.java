package org.jboss.errai.bus.server.websocket.jsr356.filter;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * In web.xml registered implementations of this filter interfaces, will be
 * invoked for each Errai message they pass through websocket.
 * 
 * @author : Michel Werren
 */
public interface WebsocketFilter {
  public void doFilter(Session websocketSession, HttpSession httpSession,
          String message);
}
