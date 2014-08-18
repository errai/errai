package org.jboss.errai.bus.server.websocket.jsr356.filter;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.util.List;
import java.util.Map;

/**
 * @author Michel Werren
 */
public class FilterDelegate {
  /**
   * Invoke filters defined in web.xml before message processing.
   * 
   * @param websocketSession
   * @param httpSession
   * @param message
   */
  public static void invokeFilterBefore(Session websocketSession, HttpSession httpSession,
          Map<Object, Object> sharedProperties, String message) {
    final List<WebSocketFilter> filters = FilterLookup.getInstance().getFilters();
    if (filters != null) {
      for (WebSocketFilter filter : filters) {
        filter.beforeProcessingMessage(websocketSession, httpSession, sharedProperties, message);
      }
    }
  }

  /**
   * Invoke filters defined in web.xml after message processing.
   * 
   * @param websocketSession
   * @param httpSession
   * @param message
   */
  public static void invokeFilterAfter(Session websocketSession, HttpSession httpSession,
          Map<Object, Object> sharedProperties, String message) {
    final List<WebSocketFilter> filters = FilterLookup.getInstance().getFilters();
    if (filters != null) {
      for (WebSocketFilter filter : filters) {
        filter.afterProcessingMessage(websocketSession, httpSession, sharedProperties, message);
      }
    }
  }
}
