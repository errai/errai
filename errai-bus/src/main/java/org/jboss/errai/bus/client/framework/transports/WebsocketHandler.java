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

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.BuiltInServices;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.ClientMessageBusImpl;
import org.jboss.errai.bus.client.framework.ClientWebSocketChannel;
import org.jboss.errai.bus.client.json.JSONUtilCli;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.client.util.BusTools;
import org.jboss.errai.common.client.api.Assert;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.util.LogUtil;

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
  private boolean hosed;

  public WebsocketHandler(final MessageCallback messageCallback, final ClientMessageBusImpl messageBus) {
    this.longPollingTransport = HttpPollingHandler.newLongPollingInstance(messageCallback, messageBus);
    this.messageCallback = Assert.notNull(messageCallback);
    this.messageBus = messageBus;
  }

  @Override
  public void configure(Message capabilitiesMessage) {
    webSocketUrl = capabilitiesMessage.get(String.class, MessageParts.WebSocketURL);
    webSocketToken = capabilitiesMessage.get(String.class, MessageParts.WebSocketToken);

    hosed = (webSocketUrl == null || webSocketToken == null);

    if (hosed) {
      LogUtil.log("server reported it supports websockets but did not send configuration information.");
    }
  }

  @Override
  public void start() {
    longPollingTransport.start();

    LogUtil.log("attempting web sockets connection at URL: " + webSocketUrl);

    final Object o = attemptWebSocketConnect(webSocketUrl);

    if (o instanceof String) {
      LogUtil.log("could not use web sockets. reason: " + o);
      hosed = true;
      InitVotes.voteFor(ClientMessageBus.class);
      messageBus.reconsiderTransport();
    }
  }

  @Override
  public void transmit(final List<Message> txMessages) {
    if (webSocketChannel != null) {
      if (!transmitToSocket(webSocketChannel, BusTools.encodeMessages(txMessages))) {
        LogUtil.log("websocket channel is closed. falling back to comet");
        messageBus.reconsiderTransport();
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

        messageBus.encodeAndTransmit(CommandMessage.createWithParts(new HashMap<String, Object>())
            .toSubject(BuiltInServices.ServerBus.name()).command(BusCommands.WebsocketChannelVerify)
            .copy(MessageParts.WebSocketToken, message));
        break;

      case WebsocketChannelOpen:
        longPollingTransport.stop();
        // send final message to open the channel
        ClientWebSocketChannel.transmitToSocket(webSocketChannel, getWebSocketNegotiationString());

        LogUtil.log("web socket channel successfully negotiated. comet channel deactivated.");
        break;

      case WebsocketNegotiationFailed:
        hosed = true;
        messageBus.reconsiderTransport();

        webSocketChannel = null;
//          displayError("failed to connect to websocket: server rejected request",
//              message.get(String.class, MessageParts.ErrorMessage), null);
        break;

    }
  }

  @Override
  public void stop() {
  }

  @Override
  public boolean isUsable() {
    return !hosed;
  }

  public void attachWebSocketChannel(final Object o) {
    LogUtil.log("web socket opened. sending negotiation message.");
    ClientWebSocketChannel.transmitToSocket(o, getWebSocketNegotiationString());
    webSocketChannel = o;
  }

  private String getWebSocketNegotiationString() {
    return "{\"" + MessageParts.CommandType.name() + "\":\"" + BusCommands.Associate.name() + "\", \""
        + MessageParts.ConnectionSessionKey + "\":\"" + messageBus.getSessionId() + "\"" + ",\"" + MessageParts.WebSocketToken
        + "\":\"" + webSocketToken + "\"}";
  }

  private void handleReceived(String json) {
    messageCallback.callback(JSONUtilCli.decodeCommandMessage(json));
  }

  public native Object attemptWebSocketConnect(final String websocketAddr) /*-{
      var messageBus = this.@org.jboss.errai.bus.client.framework.transports.WebsocketHandler::messageBus;
      var socket;
      if (window.WebSocket) {
          socket = new WebSocket(websocketAddr);

          socket.onmessage = function (event) {
              // messageBus::handleJsonMessage(Ljava/lang/String;Z)(event.data, false);
              this.@org.jboss.errai.bus.client.framework.transports.WebsocketHandler::handleReceived(Ljava/lang/String;)(event.data);
          };

          socket.onopen = function (event) {
              this.@org.jboss.errai.bus.client.framework.transports.WebsocketHandler::attachWebSocketChannel(Ljava/lang/Object;)(socket);
          };
          socket.onclose = function (event) {
              // should probably do something here.
          };
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
}
