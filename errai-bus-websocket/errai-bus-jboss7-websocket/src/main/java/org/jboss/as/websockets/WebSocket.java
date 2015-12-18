/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.as.websockets;

import org.jboss.websockets.oio.OioWebSocket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

/**
 * Represents a handle to a single WebSocket connection. It has reader and writer methods to get data in and out.
 * <p/>
 * TODO: Implement support for binary frames.
 *
 * @author Mike Brock
 */
public interface WebSocket extends OioWebSocket {


  /**
   * Return the HTTP Session with which this WebSocket is associated.
   *
   * @return an instance of the HttpSession
   */
  public HttpSession getHttpSession();


  /**
   * Get an instance of the initial ServletRequest which was responsible for opening this WebSocket. Note: that
   * this object remains the same for the duration of the WebSocket session. There is <strong>no</strong> unique
   * request associated with individual websocket frames.
   *
   * @return an instance of the HttpServletRequest which opened this WebSocket.
   */
  public HttpServletRequest getServletRequest();

}
