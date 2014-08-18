package org.jboss.errai.bus.server.websocket.jsr356.filter;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;
import java.util.Map;

/**
 * Registered implementations of this filter in web.xml will be invoked for each
 * inbound message on the WebSocket channel.
 * 
 * @author Michel Werren
 */
public interface WebSocketFilter {
  /**
   * Invoked before Errai continues processing of the message.
   * 
   * @param websocketSession
   *          actual websocket session
   * @param httpSession
   *          actual http session
   * @param sharedProperties
   *          map of user defined properties, they are kept alive during before
   *          and after filter method invocation of all filters.
   * @param message
   *          unaltered Errai message from the websocket text frame
   */
  public void beforeProcessingMessage(Session websocketSession, HttpSession httpSession,
          Map<Object, Object> sharedProperties, String message);

  /**
   * Invoked after Errai has finished processing of the message.
   * 
   * @param websocketSession
   *          actual websocket session
   * @param httpSession
   *          actual http session
   * @param sharedProperties
   *          map of user defined properties, they are kept alive during before
   *          and after filter method invocation of all filters.
   * @param message
   *          unaltered Errai message from the websocket text frame
   */
  public void afterProcessingMessage(Session websocketSession, HttpSession httpSession,
          Map<Object, Object> sharedProperties, String message);
}
