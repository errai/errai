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

import org.jboss.as.websockets.WebSocket;
import org.jboss.as.websockets.servlet.WebSocketServlet;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.io.QueueChannel;
import org.jboss.errai.bus.server.io.websockets.WebSocketServerHandler;
import org.jboss.errai.bus.server.io.websockets.WebSocketTokenManager;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJString;
import org.jboss.errai.marshalling.server.JSONDecoder;
import org.jboss.servlet.http.HttpEvent;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author Mike Brock
 */
public class JBossAS7WebSocketServlet extends WebSocketServlet {

  /* New and configured errai service */
  protected ErraiService service;

  /* A default Http session provider */
  protected SessionProvider<HttpSession> sessionProvider;

  public enum ConnectionPhase {
    NORMAL, CONNECTING, DISCONNECTING, UNKNOWN
  }

  private static final String WEBSOCKET_SESSION_ALIAS = "Websocket:Errai:SessionAlias";

  @Override
  public void init(final ServletConfig config) throws ServletException {
    //setProtocolName("J.REP1.0/ErraiBus");

    service = ServletBootstrapUtil.getService(config);
    sessionProvider = service.getSessionProvider();
  }


  @Override
  public void destroy() {
    service.stopService();
  }

  /**
   * Writes the message to the output stream
   *
   * @param stream - the stream to write to
   * @param m      - the message to write to the stream
   * @throws java.io.IOException - is thrown if any input/output errors occur while writing to the stream
   */
  public static void writeToOutputStream(OutputStream stream, MarshalledMessage m) throws IOException {
    stream.write('[');

    if (m.getMessage() == null) {
      stream.write('n');
      stream.write('u');
      stream.write('l');
      stream.write('l');
    }
    else {
      for (byte b : ((String) m.getMessage()).getBytes()) {
        stream.write(b);
      }
    }
    stream.write(']');

  }

  private static class SimpleEventChannelWrapped implements QueueChannel {
    private final WebSocket socket;

    public SimpleEventChannelWrapped(WebSocket socket) {
      this.socket = socket;
    }

    @Override
    public boolean isConnected() {
      return true;
    }

    @Override
    public void write(String data) throws IOException {
      socket.writeTextFrame(data);
    }
  }

  @Override
  protected void onSocketOpened(HttpEvent event, WebSocket socket) throws IOException {
  }

  @Override
  protected void onSocketClosed(HttpEvent event) throws IOException {
  }


  @Override
  protected void onReceivedTextFrame(HttpEvent event, final WebSocket socket) throws IOException {
    final String text = socket.readTextFrame();

    final QueueSession session = sessionProvider.getSession(event.getHttpServletRequest().getSession(),
            socket.getSocketID());

    if (text.length() == 0) return;

    @SuppressWarnings("unchecked") EJObject val = JSONDecoder.decode(text).isObject();

    final LocalContext localSessionContext = LocalContext.get(session);
    QueueSession cometSession = localSessionContext.getAttribute(QueueSession.class, WEBSOCKET_SESSION_ALIAS);

    // this is not an active channel.
    if (cometSession == null) {
      String commandType = val.get(MessageParts.CommandType.name()).isString().stringValue();

      // this client apparently wants to connect.
      if (BusCommands.ConnectToQueue.name().equals(commandType)) {
        String sessionKey = val.get(MessageParts.ConnectionSessionKey.name()).isString().stringValue();

        // has this client already attempted a connection, and is in a wait verify state
        if (sessionKey != null && (cometSession = service.getBus().getSessionBySessionId(sessionKey)) != null) {
          LocalContext localCometSession = LocalContext.get(cometSession);

          if (localCometSession.hasAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS) &&
                  WebSocketServerHandler.WEBSOCKET_ACTIVE.equals(localCometSession.getAttribute(String.class, WebSocketServerHandler.SESSION_ATTR_WS_STATUS))) {

            // set the session queue into direct channel mode.
            final QueueSession sessionRef = cometSession;
            service.getBus().getQueue(cometSession).setDirectSocketChannel(new QueueChannel() {
              @Override
              public boolean isConnected() {
                return sessionRef.isValid();
              }

              @Override
              public void write(String data) throws IOException {
                socket.writeTextFrame(data);
              }
            });

            localSessionContext.setAttribute(WEBSOCKET_SESSION_ALIAS, cometSession);
            cometSession.removeAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS);

            return;
          }

          // check the activation key matches.
          EJString activationKey = val.get(MessageParts.WebSocketToken.name()).isString();
          if (activationKey == null || !WebSocketTokenManager.verifyOneTimeToken(cometSession, activationKey.stringValue())) {

            // nope. go away!
            sendMessage(new SimpleEventChannelWrapped(socket), getFailedNegotiation("bad negotiation key"));
          }
          else {
            // the key matches. now we send the reverse challenge to prove this client is actually
            // already talking to the bus over the COMET channel.
            String reverseToken = WebSocketTokenManager.getNewOneTimeToken(cometSession);
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
      // this is an active session. send the message.;

      Message msg = MessageFactory.createCommandMessage(cometSession, text);
      service.store(msg);
    }
  }

  public static void sendMessage(QueueChannel channel, String message) throws IOException {
    channel.write(message);
  }

  private static String getFailedNegotiation(String error) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommands.WebsocketNegotiationFailed.name() + "\"," +
            "\"" + MessageParts.ErrorMessage.name() + "\":\"" + error + "\"}]";
  }

  private static String getSuccessfulNegotiation() {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommands.WebsocketChannelOpen.name() + "\"}]";
  }

  private static String getReverseChallenge(String token) {
    return "[{\"" + MessageParts.ToSubject.name() + "\":\"ClientBus\", \"" + MessageParts.CommandType.name() + "\":\""
            + BusCommands.WebsocketChannelVerify.name() + "\",\"" + MessageParts.WebSocketToken + "\":\"" +
            token + "\"}]";
  }
}