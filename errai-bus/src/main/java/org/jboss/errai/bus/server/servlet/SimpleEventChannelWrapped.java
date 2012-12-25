package org.jboss.errai.bus.server.servlet;

import org.jboss.as.websockets.WebSocket;
import org.jboss.as.websockets.frame.TextFrame;
import org.jboss.errai.bus.server.io.QueueChannel;

import java.io.IOException;

/**
* @author Mike Brock
*/
public class SimpleEventChannelWrapped implements QueueChannel {
  private final WebSocket socket;

  public SimpleEventChannelWrapped(final WebSocket socket) {
    this.socket = socket;
  }

  @Override
  public boolean isConnected() {
    return true;
  }

  @Override
  public void write(final String data) throws IOException {
    socket.writeFrame(TextFrame.from(data));
  }

  @Override
  public String getId() {
    return socket.getSocketID();
  }
}
