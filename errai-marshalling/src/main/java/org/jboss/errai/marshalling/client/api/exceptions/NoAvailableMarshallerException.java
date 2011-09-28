package org.jboss.errai.marshalling.client.api.exceptions;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class NoAvailableMarshallerException extends MarshallingException {
  private String marshallType;

  public NoAvailableMarshallerException(String marshallType) {
    super("no available marshaller for type: " + marshallType);
    this.marshallType = marshallType;
  }

  public String getMarshallType() {
    return marshallType;
  }
}
