package org.jboss.errai.bus.server.websocket.jsr356.channel;

import javax.servlet.http.HttpSession;
import javax.websocket.Session;

/**
 * Factory for {@link ErraiWebSocketChannel}.
 * 
 * @author Michel Werren
 */
public class ErraiChannelFactory {
  private static final ErraiChannelFactory INSTANCE = new ErraiChannelFactory();

  /**
   * Reference of an alternative factory to delegate the channel creation.
   */
  private static ErraiChannelFactory delegate = null;

  protected ErraiChannelFactory() {
  }

  public static ErraiChannelFactory getInstance() {
    return delegate != null ? delegate : INSTANCE;
  }

  public static void registerDelegate(ErraiChannelFactory delegate) {
    ErraiChannelFactory.delegate = delegate;
  }

  public ErraiWebSocketChannel buildWebsocketChannel(Session websocketSession, HttpSession httpSession) {
    return new DefaultErraiWebSocketChannel(websocketSession, httpSession);
  }
}
