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

package org.jboss.errai.bus.server.websocket.jsr356;

import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiChannelFactory;
import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiWebSocketChannel;
import org.jboss.errai.bus.server.websocket.jsr356.configuration.ErraiEndpointConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;

import java.util.HashMap;
import java.util.Map;

/**
 * WebSocket endpoint for Errai Bus messages.
 * 
 * @author Michel Werren
 */
@ServerEndpoint(value = "/in.erraiBusWS", configurator = ErraiEndpointConfigurator.class)
public class ErraiWebSocketEndpoint {

  public static final int CLOSE_CODE_NORMAL = 1000;
  public static final int CLOSE_CODE_AWAY = 1001;
  public static final int CLOSE_CODE_PROTOCOL_ERROR = 1002;
  public static final int CLOSE_CODE_WRONG_DATA = 1003;
  public static final int CLOSE_CODE_ABNORMAL = 1006;
  public static final int CLOSE_CODE_MESSAGE_NOT_CONSISTENT = 1007;
  public static final int CLOSE_CODE_MESSAGE_TOO_BIG = 1009;
  public static final int CLOSE_CODE_MISSING_EXTENSION = 1010;
  public static final int CLOSE_CODE_UNEXPECT_CONDITION = 1011;
  public static final int CLOSE_CODE_TLS_ERROR = 1015;

  private static final Logger LOGGER = LoggerFactory.getLogger(ErraiWebSocketEndpoint.class.getName());

  private static final Map<String, ErraiWebSocketChannel> CHANNELS = new HashMap<String, ErraiWebSocketChannel>();

  @OnOpen
  public void onOpen(Session session, EndpointConfig config) {
    final HttpSession httpSession = (HttpSession) config.getUserProperties().get(HttpSession.class.getName());

    final ErraiWebSocketChannel channel = ErraiChannelFactory.getInstance().buildWebsocketChannel(session, httpSession);
    CHANNELS.put(session.getId(), channel);
  }

  @OnMessage
  public void onMessage(String message, Session session) {
    final ErraiWebSocketChannel channel = CHANNELS.get(session.getId());
    /*
     * Fix for ERRAI-866: There is a chrome bug that can cause empty messages to
     * be sent. (See:
     * https://code.google.com/p/chromium/issues/detail?id=510016&colspec=ID%
     * 20Pri%20M%20Stars%20ReleaseBlock%20Cr%20Status%20Owner%20Summary%20OS%
     * 20Modified)
     */
    if (!message.isEmpty()) {
      channel.doErraiMessage(message);
    }
  }

  @OnClose
  public void onClose(Session session, CloseReason reason) {
    final ErraiWebSocketChannel removedChannel = CHANNELS.remove(session.getId());
    removedChannel.onSessionClosed();
    if (reason != null) {
      final int closeCode = reason.getCloseCode().getCode();
      switch (closeCode) {
      case CLOSE_CODE_ABNORMAL:
        LOGGER.debug("abnormal closing of the websocket session");
        break;
      case CLOSE_CODE_WRONG_DATA:
        LOGGER.debug("closing websocket session because of wrong data type");
        break;
      case CLOSE_CODE_PROTOCOL_ERROR:
        LOGGER.debug("closing websocket session because of protocol error");
        break;
      case CLOSE_CODE_MESSAGE_NOT_CONSISTENT:
        LOGGER.debug("closing websocket session because of not consistent data");
        break;
      case CLOSE_CODE_MESSAGE_TOO_BIG:
        LOGGER.debug("closing websocket session because of too big message");
        break;
      case CLOSE_CODE_MISSING_EXTENSION:
        LOGGER.debug("closing websocket session because of missing extension");
        break;
      case CLOSE_CODE_UNEXPECT_CONDITION:
        LOGGER.debug("closing websocket session because of unexpected condition	");
        break;
      case CLOSE_CODE_TLS_ERROR:
        LOGGER.debug("closing websocket session because of TLS handshake error");
        break;
      }
    }
  }

  @OnError
  public void onError(Throwable throwable) {
    LOGGER.error("", throwable);
  }
}
