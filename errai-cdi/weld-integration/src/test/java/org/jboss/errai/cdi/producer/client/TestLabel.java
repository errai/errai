package org.jboss.errai.cdi.producer.client;

/**
 * @author Mike Brock
 */
public class TestLabel {
  private String text;

  public TestLabel() {
  }

  public TestLabel(String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }
}
