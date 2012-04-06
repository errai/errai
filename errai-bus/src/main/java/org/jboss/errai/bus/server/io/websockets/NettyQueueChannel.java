package org.jboss.errai.bus.server.io.websockets;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.errai.bus.server.io.QueueChannel;

/**
 * @author Mike Brock
 */
public class NettyQueueChannel implements QueueChannel {
  final Channel channel;

  public NettyQueueChannel(Channel channel) {
    this.channel = channel;
  }

  @Override
  public boolean isConnected() {
    return channel.isConnected();
  }

  @Override
  public void write(String data) {
    channel.write(new TextWebSocketFrame(data));
  }
}
