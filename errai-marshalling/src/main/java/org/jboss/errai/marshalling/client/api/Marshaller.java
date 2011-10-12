package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface Marshaller<Z, T> {
  Class<T> getTypeHandled();

  String getEncodingType();

  T demarshall(Z o, MarshallingSession ctx);

  String marshall(T o, MarshallingSession ctx);

  boolean handles(Z o);
}
