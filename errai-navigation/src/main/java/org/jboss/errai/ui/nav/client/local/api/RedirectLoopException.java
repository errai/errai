package org.jboss.errai.ui.nav.client.local.api;


/**
 * This exception is thrown when the page has caused more than the maximum number of redirects ({@see org.jboss.errai.ui.nav.client.local.Navigation}), indicating an infinite redirection loop.
 * 
 * @author Divya Dadlani <ddadlani@redhat.com>
 *
 */
public class RedirectLoopException extends RuntimeException {
  private static final long serialVersionUID = 1L;

  public RedirectLoopException() {
    
  }

  public RedirectLoopException(String message) {
    super(message);
  }

  public RedirectLoopException(Throwable cause) {
    super(cause);
  }

  public RedirectLoopException(String message, Throwable cause) {
    super(message, cause);
  }

}
