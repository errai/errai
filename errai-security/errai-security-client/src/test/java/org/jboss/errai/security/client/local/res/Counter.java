package org.jboss.errai.security.client.local.res;

public class Counter {
  private int count = 0;

  public void increment() {
    count += 1;
  }

  public int getCount() {
    return count;
  }
}