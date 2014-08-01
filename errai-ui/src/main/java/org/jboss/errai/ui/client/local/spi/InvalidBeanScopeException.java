package org.jboss.errai.ui.client.local.spi;

@SuppressWarnings("serial")
public class InvalidBeanScopeException extends RuntimeException {
  public InvalidBeanScopeException() {
    super();
  }
  
  public InvalidBeanScopeException(String message) {
    super(message);
  }
  
  public InvalidBeanScopeException(String message, Throwable causes) {
    super(message, causes);
  }
}
