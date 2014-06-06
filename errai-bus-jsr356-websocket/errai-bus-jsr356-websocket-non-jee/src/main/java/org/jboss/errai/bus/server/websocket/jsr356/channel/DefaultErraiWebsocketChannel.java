package org.jboss.errai.bus.server.websocket.jsr356.channel;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SessionEndEvent;
import org.jboss.errai.bus.client.api.SessionEndListener;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceSingleton;
import org.jboss.errai.bus.server.servlet.websocket.NegotiationHandler;
import org.jboss.errai.bus.server.websocket.jsr356.filter.FilterDelegate;
import org.jboss.errai.marshalling.client.api.json.EJValue;
import org.jboss.errai.marshalling.server.JSONDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

import java.io.IOException;
import java.util.List;

/**
 * Handler for websocket messages for both ways, receiving and sending. There
 * are one instance for each {@link javax.websocket.Session} and each one has a
 * relation to its {@link javax.servlet.http.HttpSession}.
 * 
 * @author : Michel Werren
 */
public class DefaultErraiWebsocketChannel implements ErraiWebsocketChannel {

  private static final Logger LOGGER = LoggerFactory
          .getLogger(DefaultErraiWebsocketChannel.class.getName());

  protected final Session session;

  protected final ErraiService erraiService;

  protected final HttpSession httpSession;

  protected QueueSession queueSession = null;

  public DefaultErraiWebsocketChannel(Session session, HttpSession httpSession) {
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
        queueSession = NegotiationHandler.establishNegotiation(val, this,
                erraiService);
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
        FilterDelegate.invokeFilter(session, httpSession, message);
        final List<Message> commandMessages = MessageFactory
                .createCommandMessage(queueSession, val);
        erraiService.store(commandMessages);
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
