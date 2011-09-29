package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MarshallerFactory {
  Marshaller<Object, Object> getMarshaller(String formatType, String encodedType);
}

