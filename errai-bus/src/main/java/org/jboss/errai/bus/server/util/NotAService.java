package org.jboss.errai.bus.server.util;

/**
 * Thrown by {@link ServiceTypeParser} or {@link ServiceMethodParser} when the constructor is given
 * a class or member that is not a service.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class NotAService extends Exception {
  private static final long serialVersionUID = 1L;

  /**
   * @param message
   *          A message describing the cause of this exception
   */
  public NotAService(String message) {
    super(message);
  }
}