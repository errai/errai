package org.jboss.errai.marshalling.client.util;

/**
 * A simplified version of  <tt>java.enterprise.util.TypeLiteral</tt> that does complete erasure for use in
 * the marshalling API where one needs to return an erased type class instance, but the class parameterization makes
 * that impossible.
 *
 * @author Mike Brock
 */
public class SimpleTypeLiteral<T> {
  private final Class rawType;

  private SimpleTypeLiteral(final Class rawType) {
    this.rawType = rawType;
  }

  @SuppressWarnings("unchecked")
  public static <T> SimpleTypeLiteral<T> ofRawType(final Class rawType) {
    return (SimpleTypeLiteral<T>) new SimpleTypeLiteral(rawType);
  }

  @SuppressWarnings("unchecked")
  public Class<T> get() {
    return rawType;
  }
}
