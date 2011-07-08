package org.jboss.errai.ioc.tests.rebind.model;

import java.util.List;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class Foo {
  public Bar bar;
  
  public static <T> T bar(List<T> list) {
    return list.get(0);
  }
  
  public static <T> T baz(Class<T> list) {
    return null;
  }
}
