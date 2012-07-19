package org.jboss.errai.bus.client.framework;

/**
 * An interface which exposes the ability to disconnect or reconnect the client message bus. Usually in response
 * to error handling.
 * @author Mike Brock
 */
public interface BusControl {
  /**
   * Immediately disconnects the bus from the server.
   */
  public void disconnect();

  /**
   * Reconnects the bus to the server. If the bus is already connected, calling this disconnects the bus and
   * negotiates a new federation. If the bus is not connected, it attempts to initiate a new connection.
   */
  public void reconnect();
}
