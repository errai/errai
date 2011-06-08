package org.jboss.errai.common.client.types;

import java.util.HashMap;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class UHashMap<K, V> extends HashMap<K, V> {
  int hashCode = (int) (Math.random() * 100000) + super.hashCode();

  private boolean uniqueHashMode = false;

  @Override
  public int hashCode() {
    return uniqueHashMode ? hashCode : super.hashCode();
  }

  public void normalHashMode() {
    uniqueHashMode = false;
  }

  public void uniqueHashMode() {
    uniqueHashMode = true;
  }
}
