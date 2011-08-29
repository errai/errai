package org.jboss.errai.enterprise.client.cdi;

/**
* @author Mike Brock <cbrock@redhat.com>
*/
public class UnexpectedEvent {
  private Throwable exception;
  private String failure;

  public UnexpectedEvent(Throwable exception, String failure) {
    this.exception = exception;
    this.failure = failure;
  }

  public Throwable getException() {
    return exception;
  }

  public void setException(Throwable exception) {
    this.exception = exception;
  }

  public String getFailure() {
    return failure;
  }

  public void setFailure(String failure) {
    this.failure = failure;
  }
}
