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

package org.jboss.errai.bus.server.websocket.jsr356.channel;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndEvent;
import org.jboss.errai.bus.client.api.SessionEndListener;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.jboss.errai.bus.server.servlet.websocket.WebSocketNegotiationHandler;
import org.jboss.errai.bus.server.websocket.jsr356.filter.FilterDelegate;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.server.JSONDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handler for websocket messages for both receiving and sending. There is one
 * instance for each {@link javax.websocket.Session} and each one has a relation
 * to its {@link javax.servlet.http.HttpSession}.
 * 
 * @author Michel Werren
 */
public class DefaultErraiWebSocketChannel implements ErraiWebSocketChannel {

  private static final Logger LOGGER = LoggerFactory.getLogger(DefaultErraiWebSocketChannel.class.getName());

  protected final Session session;

  protected final ErraiService erraiService;

  protected final HttpSession httpSession;

  protected QueueSession queueSession = null;

  public DefaultErraiWebSocketChannel(Session session, HttpSession httpSession) {
    this.session = session;
    this.httpSession = httpSession;

    if (ErraiServiceSingleton.isInitialized()) {
      erraiService = ErraiServiceSingleton.getService();
    }
    else {
      throw new IllegalStateException(
              "Errai Bus should be initialized at this time. Default servlet configured?");
    }
  }

  public void doErraiMessage(final String message) {

    final EJValue val = JSONDecoder.decode(message);
    // this is not an active channel.
    try {
      if (queueSession == null) {
        queueSession = WebSocketNegotiationHandler.establishNegotiation(val,
                this, erraiService);
        if (queueSession != null) {
          LOGGER.trace(
                  "Negotiation done for errai session: {} on websocket session: {}",
                  queueSession.getSessionId(), session.getId());
          queueSession.addSessionEndListener(new SessionEndListener() {
            @Override
            public void onSessionEnd(SessionEndEvent event) {
              if (session.isOpen()) {
                LOGGER.warn("Errai queue session closed: {}",
                        queueSession.getSessionId());
              }
            }
          });
        }
      }
      else {
        Map<Object, Object> sharedProperties = new HashMap<Object, Object>();
        try {
          FilterDelegate.invokeFilterBefore(session, httpSession,
                  sharedProperties, message);
        } finally {
          try {
            final List<Message> commandMessages = MessageFactory
                    .createCommandMessage(queueSession, val);
            erraiService.store(commandMessages);
          } finally {
            FilterDelegate.invokeFilterAfter(session, httpSession,
                    sharedProperties, message);
          }
        }
      }
    } catch (IOException e) {
      LOGGER.error("could not proceed message", e);
    }
  }

  @Override
  public boolean isConnected() {
    return session.isOpen();
  }

  @Override
  public void write(String data) throws IOException {
    session.getBasicRemote().sendText(data);
  }

  @Override
  public void onSessionClosed() {

  }
}
