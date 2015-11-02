package org.jboss.errai.cdi.producer.client;

/**
 * @author Mike Brock
 */
public class Kayak {
  private int id;

  // for proxying
  public Kayak() {}

  public Kayak(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
