package org.jboss.errai.bus.client.framework;

import com.google.gwt.http.client.Request;

/**
 * A class representing the details of a network/transport error on the bus.
 *
 * @author Mike Brock
 */
public interface TransportError {

  /**
   * The {@link Request} associated with the error.
   * @return
   */
  public Request getRequest();

  /**
   * An error message associated with the error, if applicable. Otherwise an empty string is returned.
   * @return
   */
  public String getErrorMessage();

  /**
   * Returns true if the error occurred as the result of an HTTP request.
   * @return true if HTTP
   */
  public boolean isHTTP();

  /**
   * Returns true if the error occurred as a result of a problem with a WebSockets channel.
   * @return true if WebSockets
   */
  public boolean isWebSocket();

  /**
   * Any applicable HTTP status code with the error. Otherwise returns -1.
   * @return an HTTP status code.
   */
  public int getStatusCode();

  /**
   * Any exception associated with the error. Returns null if there's no relevant exception.
   * @return
   */
  public Throwable getException();

  /**
   * Stops any default error handling that would occur within the bus as a result of this error.
   */
  public void stopDefaultErrorHandling();

  /**
   * Returns the {@link BusControl} object, which permits shutting down or reinitialization of the bus.
   * @return a {@link BusControl} reference.
   */
  public BusControl getBusControl();
}
