package org.jboss.errai.marshalling.client.api.exceptions;

/**
 * @author Mike Brock <cbrock@redhat.com>
 */
public class MarshallingException extends RuntimeException {
  public MarshallingException() {
    super();
  }

  public MarshallingException(String s) {
    super(s);
  }

  public MarshallingException(String s, Throwable throwable) {
    super(s, throwable);
  }

  public MarshallingException(Throwable throwable) {
    super(throwable);
  }
}
