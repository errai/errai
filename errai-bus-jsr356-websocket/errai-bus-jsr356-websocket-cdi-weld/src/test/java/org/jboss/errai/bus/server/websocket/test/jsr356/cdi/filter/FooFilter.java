package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.filter;

import org.jboss.errai.bus.server.websocket.jsr356.filter.WebsocketFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * @author : Michel Werren
 */
@ApplicationScoped
public class FooFilter implements WebsocketFilter {
  @Override
  public void doFilter(Session websocketSession, HttpSession httpSession,
          String message) {

  }
}
