package org.jboss.errai.marshalling.client.api;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public interface MarshallerFactory {
  /**
   * Returns a {@link Marshaller} capable of handling the specified encodedType
   * @param formatType A format type of the serialized object.
   * @param encodedType The fully-qualified Java class name of the encoded type
   * @return a marshaller instance.
   */
  Marshaller<Object, Object> getMarshaller(String formatType, String encodedType);
}

