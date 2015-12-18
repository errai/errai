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

package org.jboss.errai.bus.server.servlet.websocket;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.protocols.BusCommand;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.io.DirectDeliveryHandler;
import org.jboss.errai.bus.server.io.QueueChannel;
import org.jboss.errai.bus.server.io.websockets.WebSocketServerHandler;
import org.jboss.errai.bus.server.io.websockets.WebSocketTokenManager;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.util.LocalContext;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.client.api.json.EJObject;
import org.jboss.errai.marshalling.client.api.json.EJString;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Responsible for establishing a WebSocket connection with clients.
 * 
 * @author Michel Werren
 */
public class WebSocketNegotiationHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketNegotiationHandler.class.getName());

  @SuppressWarnings("rawtypes")
  public static QueueSession establishNegotiation(EJValue val, QueueChannel queueChannel, ErraiService service)
          throws IOException {

    QueueSession session = null;
    final EJObject ejObject = val.isObject();
    if (ejObject == null) {
      return null;
    }

    final String commandType = ejObject.get(MessageParts.CommandType.name()).isString().stringValue();

    // this client apparently wants to connect.
    if (BusCommand.Associate.name().equals(commandType)) {
      final String sessionKey = ejObject.get(MessageParts.ConnectionSessionKey.name()).isString().stringValue();

      // has this client already attempted a connection, and is in a wait verify
      // state
      if (sessionKey != null && (session = service.getBus().getSessionBySessionId(sessionKey)) != null) {
        final LocalContext localCometSession = LocalContext.get(session);

        if (localCometSession.hasAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS)
                && WebSocketServerHandler.WEBSOCKET_ACTIVE.equals(localCometSession.getAttribute(String.class,
                        WebSocketServerHandler.SESSION_ATTR_WS_STATUS))) {

          // set the session queue into direct channel mode.
          final MessageQueue queue = service.getBus().getQueueBySession(sessionKey);
          queue.setDeliveryHandler(DirectDeliveryHandler.createFor(queueChannel));
          LOGGER.debug("set direct delivery handler on session: {}", session.getSessionId());

          //See ERRAI-873: In case a connection failure has occurred make sure 
          //to resend a successful negotiation message.
          sendMessage(queueChannel, WebSocketNegotiationMessage.getSuccessfulNegotiation());

          return session;
        }

        // check the activation key matches.
        final EJString activationKey = ejObject.get(MessageParts.WebSocketToken.name()).isString();
        if (activationKey == null || !WebSocketTokenManager.verifyOneTimeToken(session, activationKey.stringValue())) {

          // nope. go away!
          final String error = "bad negotiation key";
          LOGGER.debug("activation key not match for session: {}", session.getSessionId());
          sendMessage(queueChannel, WebSocketNegotiationMessage.getFailedNegotiation(error));
        }
        else {
          // the key matches. now we send the reverse challenge to prove this
          // client is actually
          // already talking to the bus over the COMET channel.
          final String reverseToken = WebSocketTokenManager.getNewOneTimeToken(session);
          localCometSession.setAttribute(WebSocketServerHandler.SESSION_ATTR_WS_STATUS,
                  WebSocketServerHandler.WEBSOCKET_AWAIT_ACTIVATION);

          // send the challenge.
          LOGGER.debug("reverse challange for session: {}", session.getSessionId());
          sendMessage(queueChannel, WebSocketNegotiationMessage.getReverseChallenge(reverseToken));
          return null;
        }
        sendMessage(queueChannel, WebSocketNegotiationMessage.getSuccessfulNegotiation());
      }
      else {
        final String error = "bad session id";
        LOGGER.debug("bad session id");
        sendMessage(queueChannel, WebSocketNegotiationMessage.getFailedNegotiation(error));
      }
    }
    else {
      final String error = "bad command";
      LOGGER.debug("bad command");
      sendMessage(queueChannel, WebSocketNegotiationMessage.getFailedNegotiation(error));
    }
    return null;
  }

  public static void sendMessage(final QueueChannel channel, final String message) throws IOException {
    channel.write(message);
  }
}
