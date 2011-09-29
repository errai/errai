package org.jboss.errai.marshalling.client.util;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallUtil {
  public static <T> T notNull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }
}
