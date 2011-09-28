package org.jboss.errai.marshalling.client.api.exceptions;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class NoAvailableMarshallerException extends MarshallingException {
  private Class<?> marshallType;

  public NoAvailableMarshallerException(Class<?> marshallType) {
    super("no available marshaller for type: " + marshallType.getName());
    this.marshallType = marshallType;
  }

  public Class<?> getMarshallType() {
    return marshallType;
  }
}
