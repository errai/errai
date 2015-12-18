package org.jboss.errai.bus.server.websocket.test.jsr356.cdi.filter;

import org.jboss.errai.bus.server.websocket.jsr356.filter.WebSocketFilter;

import javax.enterprise.context.ApplicationScoped;
import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.util.Map;

/**
 * @author Michel Werren
 */
@ApplicationScoped
public class BarFilter implements WebSocketFilter {
  @Override
  public void beforeProcessingMessage(Session websocketSession, HttpSession httpSession,
          Map<Object, Object> sharedProperties, String message) {
  }

  @Override
  public void afterProcessingMessage(Session websocketSession, HttpSession httpSession,
          Map<Object, Object> sharedProperties, String message) {
  }
}
