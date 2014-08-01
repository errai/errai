package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.filter;

import org.jboss.errai.bus.server.websocket.jsr356.filter.WebSocketFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * @author Michel Werren
 */
@ApplicationScoped
public class BarFilter implements WebSocketFilter {
  @Override
  public void doFilter(Session websocketSession, HttpSession httpSession, String message) {

  }
}
