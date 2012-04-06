package org.jboss.errai.bus.server.io;

/**
 * @author Mike Brock
 */
public interface QueueChannel {
  public boolean isConnected();
  public void write(String data);
}
