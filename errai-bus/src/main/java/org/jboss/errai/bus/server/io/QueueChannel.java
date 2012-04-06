package org.jboss.errai.bus.server.io;

import java.io.IOException;

/**
 * @author Mike Brock
 */
public interface QueueChannel {
  public boolean isConnected();
  public void write(String data) throws IOException;
}
