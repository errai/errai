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

package org.jboss.errai.bus.server.servlet;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jboss.as.websockets.Frame;
import org.jboss.as.websockets.FrameType;
import org.jboss.as.websockets.WebSocket;
import org.jboss.as.websockets.frame.PongFrame;
import org.jboss.as.websockets.frame.TextFrame;
import org.jboss.as.websockets.servlet.WebSocketServlet;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.servlet.websocket.WebSocketNegotiationHandler;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.server.JSONDecoder;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JBossAS7WebSocketServlet extends WebSocketServlet {

  private static final long serialVersionUID = 1852376101049145649L;

  /* New and configured errai service */
  protected ErraiService service;

  /* A default Http session provider */
  protected SessionProvider<HttpSession> sessionProvider;
  private final Map<WebSocket, QueueSession> activeChannels = new ConcurrentHashMap<WebSocket, QueueSession>();

  public JBossAS7WebSocketServlet() {
    super("J.REP1.0/ErraiBus");
  }

  @SuppressWarnings("unchecked")
  @Override
  public void init(final ServletConfig config) throws ServletException {
    service = ServletBootstrapUtil.getService(config);
    sessionProvider = service.getSessionProvider();
  }

  @Override
  public void destroy() {
    activeChannels.clear();
    service.stopService();
  }

  @Override
  protected void onSocketOpened(final WebSocket socket) throws IOException {
  }

  @Override
  protected void onSocketClosed(final WebSocket socket) throws IOException {
    QueueSession session = activeChannels.remove(socket);
    service.getBus().getQueue(session).setDeliveryHandlerToDefault();
  }

  @Override
  protected void onReceivedFrame(final WebSocket socket) throws IOException {
    final Frame frame = socket.readFrame();
    switch (frame.getType()) {
    case Ping:
      socket.writeFrame(new PongFrame());
      break;
    case Binary:
      socket.writeFrame(TextFrame.from("Binary Frames Not Supported!"));
      break;
    default:
      if (frame.getType() != FrameType.Text) {
        return;
      }
    }

    final EJValue val = JSONDecoder.decode(((TextFrame) frame).getText());
    // this is not an active channel.
    if (!activeChannels.containsKey(socket)) {
      final QueueSession queueSession = 
              WebSocketNegotiationHandler.establishNegotiation(val, new SimpleEventChannelWrapped(socket), service);
      
      if (queueSession != null) {
        activeChannels.put(socket, queueSession);
      }
    }
    else {
      QueueSession queueSession = activeChannels.get(socket);
      // this is an active session. send the message.
      for (final Message msg : MessageFactory.createCommandMessage(queueSession, val)) {
        msg.setResource(HttpServletRequest.class.getName(), socket.getServletRequest());
        service.store(msg);
      }
    }
  }
}
