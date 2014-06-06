package org.jboss.errai.bus.server.websocket.test.jsr356.filter;

import org.jboss.errai.bus.server.websocket.jsr356.filter.WebsocketFilter;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * @author : Michel Werren
 * @since : 6/4/14 / 9:51 PM
 */
public class BarFilter implements WebsocketFilter {
  @Override
  public void doFilter(Session websocketSession, HttpSession httpSession,
          String message) {
  }
}
