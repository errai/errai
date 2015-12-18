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

package org.jboss.as.websockets.servlet;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.as.websockets.WebSocket;
import org.jboss.servlet.http.HttpEvent;
import org.jboss.servlet.http.HttpEventServlet;
import org.jboss.servlet.http.UpgradableHttpServletResponse;
import org.jboss.websockets.oio.ClosingStrategy;
import org.jboss.websockets.oio.HttpRequestBridge;
import org.jboss.websockets.oio.HttpResponseBridge;
import org.jboss.websockets.oio.OioWebSocket;
import org.jboss.websockets.oio.WebSocketConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A very, very early and experimental spike to get websockets working in JBoss AS. Designed for JBoss AS 7.1.2 and
 * later.
 *
 * @author Mike Brock
 */
public abstract class WebSocketServlet extends HttpServlet implements HttpEventServlet {
  private static final Logger log = LoggerFactory.getLogger(WebSocketServlet.class);

  private final String protocolName;

  /**
   * Set the protocol name to be returned in the Sec-WebSocket-Protocol header attribute during negotiation.
   *
   * @param protocolName the protocol string to be advertised in the Sec-WebSocket-Protocol header when clients negotiate
   *                     a new websocket.
   */
  protected WebSocketServlet(String protocolName) {
    this.protocolName = protocolName;
  }

  protected WebSocketServlet() {
    this.protocolName = null;
  }

  /**
   * An attribute name to stuff WebSocket handles into the request attributes with.
   */
  private static final String SESSION_WEBSOCKET_HANDLE = "JBoss:AS:WebSocket:Handle";

  /**
   * Handle an event from the web container.
   *
   * @param event
   * @throws IOException
   * @throws ServletException
   */
  public final void event(final HttpEvent event) throws IOException, ServletException {
    final HttpServletRequest request = event.getHttpServletRequest();
    final HttpServletResponse response = event.getHttpServletResponse();

    switch (event.getType()) {
      case BEGIN:
        event.setTimeout(20000);

        /**
         * Check to see if this request is an HTTP Upgrade request.
         */
        if (response instanceof UpgradableHttpServletResponse) {
          HttpRequestBridge requestBridge = new HttpRequestBridge() {
            public String getHeader(String name) {
              return request.getHeader(name);
            }

            public String getRequestURI() {
              return request.getRequestURI();
            }

            public InputStream getInputStream() {
              try {
                return request.getInputStream();
              }
              catch (IOException e) {
                throw new RuntimeException(e);
              }
            }
          };

          HttpResponseBridge responseBridge = new HttpResponseBridge() {
            public String getHeader(String name) {
              return response.getHeader(name);
            }

            public void setHeader(String name, String val) {
              response.setHeader(name, val);
            }

            public OutputStream getOutputStream() {
              try {
                return response.getOutputStream();
              }
              catch (IOException e) {
                throw new RuntimeException(e);
              }
            }

            public void startUpgrade() {
              ((UpgradableHttpServletResponse) response).startUpgrade();
            }

            public void sendUpgrade() throws IOException {
              ((UpgradableHttpServletResponse) response).sendUpgrade();
            }
          };
          OioWebSocket oioWebSocket = WebSocketConnectionManager.establish(protocolName, requestBridge, responseBridge,
                  new ClosingStrategy() {
                    public void doClose() throws IOException {
                      onSocketClosed((WebSocket) request.getAttribute(SESSION_WEBSOCKET_HANDLE));
                      event.close();
                    }
                  });
          WebSocket webSocket = new WebSocketDelegate(request, oioWebSocket);
          request.setAttribute(SESSION_WEBSOCKET_HANDLE, webSocket);
          onSocketOpened(webSocket);
        }
        else {
          throw new IllegalStateException("cannot upgrade connection");
        }
        break;
      case END:
        break;
      case ERROR:
        onSocketClosed((WebSocket) request.getAttribute(SESSION_WEBSOCKET_HANDLE));
        event.close();
        break;
      case EVENT:
      case READ:
        while (event.isReadReady()) {
          onReceivedFrame((WebSocket) request.getAttribute(SESSION_WEBSOCKET_HANDLE));
        }
        break;

      case TIMEOUT:
        event.resume();
        break;

      case EOF:
        onSocketClosed((WebSocket) request.getAttribute(SESSION_WEBSOCKET_HANDLE));
        break;

    }
  }

  //
  // Override all the normal HTTP methods and make them final so they can't be inherited by users of this servlet.
  //
  @Override
  protected final void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doGet(req, resp);
  }

  @Override
  protected final long getLastModified(HttpServletRequest req) {
    return super.getLastModified(req);
  }

  @Override
  protected final void doHead(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doHead(req, resp);
  }

  @Override
  protected final void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPost(req, resp);
  }

  @Override
  protected final void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doPut(req, resp);
  }

  @Override
  protected final void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doDelete(req, resp);
  }

  @Override
  protected final void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doOptions(req, resp);
  }

  @Override
  protected final void doTrace(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.doTrace(req, resp);
  }

  @Override
  protected final void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
    super.service(req, resp);
  }

  @Override
  public final void service(ServletRequest req, ServletResponse res) throws ServletException, IOException {
    super.service(req, res);
  }
  //
  //  Finish overriding methods
  //

  /**
   * Called when a new websocket is opened.
   *
   * @param socket A reference to the WebSocket writer interface
   * @throws IOException
   */
  protected void onSocketOpened(final WebSocket socket) throws IOException {
  }

  /**
   * Called when the websocket is closed.
   *
   * @throws IOException
   */
  protected void onSocketClosed(final WebSocket socket) throws IOException {
  }

  /**
   * Called when a new text frame is received.
   *
   * @param socket A reference to the WebSocket writer interface associated with this socket.
   * @throws IOException
   */
  protected void onReceivedFrame(final WebSocket socket) throws IOException {
  }
}
