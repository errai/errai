package org.jboss.errai.bus.client.api.base;

public class MessageCallbackFailure extends RuntimeException {

  private static final long serialVersionUID = 1L;

  public MessageCallbackFailure(Throwable throwable) {
    super(throwable);
  }

}
