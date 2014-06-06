package org.jboss.errai.bus.server.websocket.jsr356.weld.channel;

import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiChannelFactory;
import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiWebsocketChannel;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * Cdi version of the {@link ErraiChannelFactory}
 * 
 * @author : Michel Werren
 */
public class CdiErraiChannelFactory extends ErraiChannelFactory {

  public static final CdiErraiChannelFactory INSTANCE = new CdiErraiChannelFactory();

  private CdiErraiChannelFactory() {
  }

  public static CdiErraiChannelFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ErraiWebsocketChannel buildWebsocketChannel(Session websocketSession,
          HttpSession httpSession) {
    return new CdiErraiWebsocketChannel(websocketSession, httpSession);
  }
}
