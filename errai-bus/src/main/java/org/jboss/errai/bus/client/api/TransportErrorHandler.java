package org.jboss.errai.bus.client.api;

/**
 * A transport error handler is used for handling errors which arise from network communication problems between the
 * client bus and the remote server bus.
 *
 * @author Mike Brock
 */
public interface TransportErrorHandler {
  /**
   * Called by the bus upon an error that occurs during communication.
   *
   * @param error an {@link TransportError} containing details of the error and also error handling functionality.
   */
  public void onError(TransportError error);
}
