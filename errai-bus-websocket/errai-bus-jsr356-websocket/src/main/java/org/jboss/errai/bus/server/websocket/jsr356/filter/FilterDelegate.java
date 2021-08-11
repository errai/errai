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
