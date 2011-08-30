package org.jboss.errai.codegen.framework.tests.model;

import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class Foo {
  public Bar bar;
  
  public static <T> T foo(T t) {
    return t;
  }
  
  public static <T> T bar(List<T> list) {
    return list.get(0);
  }

  public static <T, V> T bar(int n, List<List<Map<T, V>>> list) {
    return null;
  }

  public static <K, V> V bar(Map<K, V> map) {
    return map.get(null);
  }
  
  public static <T> T baz(Class<T> list) {
    return null;
  }
}
