package org.jboss.errai.bus.server.websocket.test.jsr356.filter;

import org.jboss.errai.bus.server.websocket.jsr356.filter.WebSocketFilter;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * @author Michel Werren
 */
public class BarFilter implements WebSocketFilter {
  @Override
  public void doFilter(Session websocketSession, HttpSession httpSession, String message) {
  }
}
