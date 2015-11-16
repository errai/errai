package org.jboss.errai.cdi.producer.client;

/**
 * @author Mike Brock
 */
public class Fooblie {
  private final String type;

  public Fooblie(String type) {
    this.type = type;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "Fooblie{" +
            "type='" + type + '\'' +
            '}';
  }
}
