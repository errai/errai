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

package org.jboss.errai.bus.client.framework.transports;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.BuiltInServices;
import org.jboss.errai.bus.client.framework.BusState;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.util.BusToolsCli;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.LogUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * @author Mike Brock
 */
public class WebsocketHandler implements TransportHandler {
  private final MessageCallback messageCallback;
  private final ClientMessageBusImpl messageBus;

  private String webSocketUrl;
  private String webSocketToken;
  private Object webSocketChannel;
  private HttpPollingHandler longPollingTransport;

  private boolean configured;
  private boolean hosed;
  private boolean stopped;

  private int retries;

  public WebsocketHandler(final MessageCallback messageCallback, final ClientMessageBusImpl messageBus) {
    this.longPollingTransport = HttpPollingHandler.newLongPollingInstance(messageCallback, messageBus);
    this.messageCallback = Assert.notNull(messageCallback);
    this.messageBus = messageBus;
  }

  @Override
  public void configure(final Message capabilitiesMessage) {
    if (!isWebSocketSupported()) {
      LogUtil.log("websockets not supported by browser");
      hosed = true;
      return;
    }

    webSocketUrl = capabilitiesMessage.get(String.class, MessageParts.WebSocketURL);
    webSocketToken = capabilitiesMessage.get(String.class, MessageParts.WebSocketToken);

    hosed = (webSocketUrl == null || webSocketToken == null);

    if (hosed) {
      LogUtil.log("server reported it supports websockets but did not send configuration information.");
    }
    else {
      configured = true;
    }
  }

  @Override
  public void start() {
    if (webSocketChannel != null && isConnected(webSocketChannel)) {
      return;
    }

    longPollingTransport.start();

    LogUtil.log("attempting web sockets connection at URL: " + webSocketUrl);

    final Object o = attemptWebSocketConnect(webSocketUrl);

    if (o instanceof String) {
      LogUtil.log("could not use web sockets. reason: " + o);
      hosed = true;
      messageBus.reconsiderTransport();
    }
  }

  @Override
  public void transmit(final List<Message> txMessages) {
    if (webSocketChannel != null) {
      if (!transmitToSocket(webSocketChannel, BusToolsCli.encodeMessages(txMessages))) {
        longPollingTransport.transmit(txMessages);
//        LogUtil.log("websocket channel is closed. falling back to comet");
//        messageBus.reconsiderTransport();
      }
    }
    else {
      longPollingTransport.transmit(txMessages);
    }
  }

  @Override
  public void handleProtocolExtension(final Message message) {
    switch (BusCommands.valueOf(message.getCommandType())) {
      case WebsocketChannelVerify:
        LogUtil.log("received verification token for websocket connection");

        longPollingTransport.transmit(Collections.singletonList(CommandMessage.createWithParts(new HashMap<String, Object>())
            .toSubject(BuiltInServices.ServerBus.name()).command(BusCommands.WebsocketChannelVerify)
            .copy(MessageParts.WebSocketToken, message)));

        break;

      case WebsocketChannelOpen:
        if (messageBus.getState() == BusState.CONNECTION_INTERRUPTED)
          messageBus.setState(BusState.CONNECTED);

        longPollingTransport.stop(false);
        // send final message to open the channel
        transmitToSocket(webSocketChannel, getWebSocketNegotiationString());

        webSocketToken = message.get(String.class, MessageParts.WebSocketToken);

        LogUtil.log("web socket channel successfully negotiated. comet channel deactivated. (reconnect token: " + webSocketToken + ")");

        retries = 0;
        break;

      case WebsocketNegotiationFailed:
        hosed = true;
        messageBus.reconsiderTransport();

        webSocketChannel = null;
        break;
    }
  }

  @Override
  public Collection<Message> stop(final boolean stopAllCurrentRequests) {
    longPollingTransport.stop(stopAllCurrentRequests);
    if (webSocketChannel != null) {
      disconnectSocket(webSocketChannel);
    }
    stopped = true;
    return Collections.emptyList();
  }

  @Override
  public boolean isUsable() {
    return configured && !hosed;
  }

  public void attachWebSocketChannel(final Object o) {
    LogUtil.log("web socket opened. sending negotiation message.");
    transmitToSocket(o, getWebSocketNegotiationString());
    webSocketChannel = o;
  }

  private String getWebSocketNegotiationString() {
    return "{\"" + MessageParts.CommandType.name() + "\":\"" + BusCommands.Associate.name() + "\", \""
        + MessageParts.ConnectionSessionKey + "\":\"" + messageBus.getSessionId() + "\"" + ",\"" + MessageParts.WebSocketToken
        + "\":\"" + webSocketToken + "\"}";
  }

  private void handleReceived(String json) {
    BusToolsCli.decodeToCallback(json, messageCallback);
  }

  public String toString() {
    return "WebSockets";
  }

  public native void disconnectSocket(final Object channel) /*-{
      channel.close();
  }-*/;

  public native static boolean isWebSocketSupported() /*-{
      return !!window.WebSocket;
  }-*/;

  public native Object attemptWebSocketConnect(final String websocketAddr) /*-{
      var thisRef = this;
      var socket;
      if (window.WebSocket) {
          socket = new WebSocket(websocketAddr);

          socket.onmessage = function (event) {
              thisRef.@org.jboss.errai.bus.client.framework.transports.WebsocketHandler::handleReceived(Ljava/lang/String;)(event.data);
          };

          socket.onopen = function (event) {
              thisRef.@org.jboss.errai.bus.client.framework.transports.WebsocketHandler::attachWebSocketChannel(Ljava/lang/Object;)(socket);
          };
          socket.onclose = function (event) {
              thisRef.@org.jboss.errai.bus.client.framework.transports.WebsocketHandler::notifyDisconnected()();

              // should probably do something here.
          };

          socket.onerror = function (event) {
              if (event.srcElement.readyState != WebSocket.OPEN) {
                  thisRef.@org.jboss.errai.bus.client.framework.transports.WebsocketHandler::notifyDisconnected()();
              }
          }
          return socket;
      } else {
          return "NotSupportedByBrowser";
      }
  }-*/;

  public native boolean transmitToSocket(final Object socket, final String text) /*-{
      if (socket.readyState == WebSocket.OPEN) {
          socket.send(text);
          return true;
      }
      else {
          return false;
      }
  }-*/;

  public native static boolean isConnected(final Object socket) /*-{
      return socket.readyState == WebSocket.OPEN;
  }-*/;

  private void notifyDisconnected() {
    LogUtil.log("websocket disconnected");
    messageBus.setState(BusState.CONNECTION_INTERRUPTED);
    disconnectSocket(webSocketChannel);
    webSocketChannel = null;

    if (!stopped) {
      retries++;
      new Timer() {
        @Override
        public void run() {
          LogUtil.log("attempting reconnection ... ");
          longPollingTransport.stop(false);
          start();
        }
      }.schedule(retries * 1000);
    }
  }
}
