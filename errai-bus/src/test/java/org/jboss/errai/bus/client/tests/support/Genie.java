package org.jboss.errai.bus.client.tests.support;

import org.jboss.errai.common.client.api.annotations.Portable;

/**
 * @author Mike Brock
 */
@Portable
public class Genie<T> {
  private int x;

  public int getX() { return x; }
  public void setX(final int x) { this.x = x; }
}