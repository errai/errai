package org.jboss.websockets.oio;

import java.io.IOException;

/**
 * A closing strategy represents the strategy with which a socket should terminate its connection with the client,
 * if a condition arises that requires termination of the socket.
 *
 * @author Mike Brock
 */
public interface ClosingStrategy {
  /**
   * Close the socket.
   */
  public void doClose() throws IOException;
}
