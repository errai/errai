package org.jboss.errai.cdi.producer.client;

/**
 * @author Mike Brock
 */
public class FooLabel {
  private String text;

  public FooLabel() {
  }

  public FooLabel(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
