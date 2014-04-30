/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
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
import org.jboss.errai.bus.client.protocols.BusCommand;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.io.DirectDeliveryHandler;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.io.QueueChannel;
import org.jboss.errai.bus.server.io.websockets.WebSocketServerHandler;
import org.jboss.errai.bus.server.io.websockets.WebSocketTokenManager;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJString;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.server.JSONDecoder;

/**
 * @author Mike Brock
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class JBossAS7WebSocketServlet extends WebSocketServlet {

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

    final QueueSession session;
    final EJValue val = JSONDecoder.decode(((TextFrame) frame).getText());
    // this is not an active channel.
    if (!activeChannels.containsKey(socket)) {
      final EJObject ejObject = val.isObject();
      if (ejObject == null) {
        return;
      }

      final String commandType = ejObject.get(MessageParts.CommandType.name()).isString().stringValue();

      // this client apparently wants to connect.
      if (BusCommand.Associate.name().equals(commandType)) {
        final String sessionKey = ejObject.get(MessageParts.ConnectionSessionKey.name()).isString().stringValue();

        // has this client already attempted a connection, and is in a wait verify state
        if (sessionKey != null && (session = service.getBus().getSessionBySessionId(sessionKey)) != null) {
          final LocalContext localCometSession = LocalContext.get(session);

          if (localCometSession.hasAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS) &&
                  WebSocketServerHandler.WEBSOCKET_ACTIVE.equals(localCometSession.getAttribute(String.class, WebSocketServerHandler.SESSION_ATTR_WS_STATUS))) {

            // set the session queue into direct channel mode.
            final MessageQueue queue = service.getBus().getQueueBySession(sessionKey);
            queue.setDeliveryHandler(DirectDeliveryHandler.createFor(new SimpleEventChannelWrapped(socket)));
            activeChannels.put(socket, session);
            return;
          }

          // check the activation key matches.
          final EJString activationKey = ejObject.get(MessageParts.WebSocketToken.name()).isString();
          if (activationKey == null || !WebSocketTokenManager.verifyOneTimeToken(session, activationKey.stringValue())) {

            // nope. go away!
            sendMessage(new SimpleEventChannelWrapped(socket), getFailedNegotiation("bad negotiation key"));
          }
          else {
            // the key matches. now we send the reverse challenge to prove this client is actually
            // already talking to the bus over the COMET channel.
            final String reverseToken = WebSocketTokenManager.getNewOneTimeToken(session);
            localCometSession.setAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS, WebSocketServerHandler.WEBSOCKET_AWAIT_ACTIVATION);

            // send the challenge.
            sendMessage(new SimpleEventChannelWrapped(socket), getReverseChallenge(reverseToken));
            return;
          }
          sendMessage(new SimpleEventChannelWrapped(socket), getSuccessfulNegotiation());
        }
        else {
          sendMessage(new SimpleEventChannelWrapped(socket), getFailedNegotiation("bad session id"));
        }
      }
      else {
        sendMessage(new SimpleEventChannelWrapped(socket), getFailedNegotiation("bad command"));
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

  public static void sendMessage(final QueueChannel channel, final String message) throws IOException {
    channel.write(message);
  }

  private static String getFailedNegotiation(final String error) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommand.WebsocketNegotiationFailed.name() + "\"," +
            "\"" + MessageParts.ErrorMessage.name() + "\":\"" + error + "\"}]";
  }

  private static String getSuccessfulNegotiation() {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommand.WebsocketChannelOpen.name() + "\"}]";
  }

  private static String getReverseChallenge(final String token) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommand.WebsocketChannelVerify.name() + "\",\"" + MessageParts.WebSocketToken + "\":\"" +
            token + "\"}]";
  }
}