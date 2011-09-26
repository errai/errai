package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MarshallerFactory {
  <T> Marshaller<?, T> getMarshaller(String encodingType, Class<T> encodedType);
}
