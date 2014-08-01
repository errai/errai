package org.jboss.websockets.oio.internal.util;

/**
 * @author Mike Brock
 */
public final class Assert {
  private Assert() {
  }

  public static <T> T notNull(final T value, final String errorIfNull) {
    if (value == null) {
      throw new AssertionError(errorIfNull);
    }
    return value;
  }
}
