package org.jboss.errai.ioc.client.container;

/**
 * A simple tuple implementation meant to hold a key-value pair.
 *
* @author Mike Brock
*/
public class Tuple<K, V> {
  private final K key;
  private final V value;

  private Tuple(final K key, final V value) {
    this.key = key;
    this.value = value;
  }

  /**
   * Creates a new Tuple. Either the key or value can be null.
   *
   * @param k the key
   * @param v the value
   * @param <K> the key type
   * @param <V> the value type
   * @return an instance of Tuple.
   */
  public static <K, V> Tuple<K, V> of(final K k, final V v) {
    return new Tuple<K, V>(k, v);
  }

  /**
   * Returns the key. May be null.
   * @return
   */
  public K getKey() {
    return key;
  }

  /**
   * Returns the value. May be null.
   * @return
   */
  public V getValue() {
    return value;
  }

  public String toString() {
    return "[" + getKey() + ", " + getValue() + "]";
  }
}
