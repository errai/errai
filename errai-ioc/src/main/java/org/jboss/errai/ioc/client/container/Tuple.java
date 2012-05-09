package org.jboss.errai.ioc.client.container;

/**
* @author Mike Brock
*/
public class Tuple<K, V> {
  private final K key;
  private final V value;

  Tuple(K key, V value) {
    this.key = key;
    this.value = value;
  }

  public static <K, V> Tuple<K, V> of(K k, V v) {
    return new Tuple<K, V>(k, v);
  }


  public K getKey() {
    return key;
  }

  public V getValue() {
    return value;
  }
}
