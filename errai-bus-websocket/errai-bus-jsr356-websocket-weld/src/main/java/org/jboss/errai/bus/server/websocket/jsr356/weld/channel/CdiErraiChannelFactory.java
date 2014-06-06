package org.jboss.errai.bus.server.websocket.jsr356.weld.channel;

import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiChannelFactory;
import org.jboss.errai.bus.server.websocket.jsr356.channel.ErraiWebSocketChannel;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * CDI version of the {@link ErraiChannelFactory}
 * 
 * @author Michel Werren
 */
public class CdiErraiChannelFactory extends ErraiChannelFactory {

  public static final CdiErraiChannelFactory INSTANCE = new CdiErraiChannelFactory();

  private CdiErraiChannelFactory() {
  }

  public static CdiErraiChannelFactory getInstance() {
    return INSTANCE;
  }

  @Override
  public ErraiWebSocketChannel buildWebsocketChannel(Session websocketSession, HttpSession httpSession) {
    return new CdiErraiWebSocketChannel(websocketSession, httpSession);
  }
}
