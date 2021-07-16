/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
