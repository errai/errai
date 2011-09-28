package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface Marshaller<Z, T> {
  Class<T> getTypeHandled();

  String getEncodingType();

  T demarshall(Z o, MarshallingContext ctx);

  String marshall(T o, MarshallingContext ctx);

  boolean handles(Z o);
}
