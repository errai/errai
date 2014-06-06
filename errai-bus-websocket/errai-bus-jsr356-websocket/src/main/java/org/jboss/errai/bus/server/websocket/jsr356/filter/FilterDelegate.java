package org.jboss.errai.bus.server.websocket.jsr356.filter;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import java.util.List;

/**
 * @author Michel Werren
 */
public class FilterDelegate {
  /**
   * Invoke filters defined in web.xml.
   * 
   * @param websocketSession
   * @param httpSession
   * @param message
   */
  public static void invokeFilter(Session websocketSession, HttpSession httpSession, String message) {
    final List<WebSocketFilter> filters = FilterLookup.getInstance().getFilters();
    if (filters != null) {
      for (WebSocketFilter filter : filters) {
        filter.doFilter(websocketSession, httpSession, message);
      }
    }
  }
}
