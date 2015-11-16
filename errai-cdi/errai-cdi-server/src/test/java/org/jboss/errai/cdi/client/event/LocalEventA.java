package org.jboss.errai.cdi.client.event;

/**
 * @author Mike Brock
 */
public class LocalEventA {
  private String message;

  public LocalEventA(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }

  @Override
  public String toString() {
    return "LocalEventA{" +
        "message='" + message + '\'' +
        '}';
  }
}
