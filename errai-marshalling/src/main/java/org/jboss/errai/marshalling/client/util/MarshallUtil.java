package org.jboss.errai.marshalling.client.util;

/**
 * @author Mike Brock <cbrock@redhat.com>
 * @author Jonathan Fuerth <jfuerth@redhat.com>
 */
public class MarshallUtil {
  public static <T> T notNull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }
}
