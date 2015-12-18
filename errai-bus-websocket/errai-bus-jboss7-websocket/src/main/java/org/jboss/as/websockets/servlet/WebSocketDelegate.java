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

package org.jboss.as.websockets.servlet;

import org.jboss.as.websockets.Frame;
import org.jboss.as.websockets.WebSocket;
import org.jboss.websockets.oio.OioWebSocket;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.io.IOException;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
class WebSocketDelegate implements WebSocket {
  protected HttpServletRequest request;
  protected OioWebSocket delegate;

  public WebSocketDelegate(HttpServletRequest request, OioWebSocket delegate) {
    this.request = request;
    this.delegate = delegate;
  }

  public HttpSession getHttpSession() {
    return request.getSession();
  }

  public HttpServletRequest getServletRequest() {
    return request;
  }

  public String getSocketID() {
    return delegate.getSocketID();
  }

  public Frame readFrame() throws IOException {
    return delegate.readFrame();
  }

  public void writeFrame(Frame frame) throws IOException {
    delegate.writeFrame(frame);
  }

  public void closeSocket() throws IOException {
    delegate.closeSocket();
  }
}
