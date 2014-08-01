package org.jboss.errai.ui.nav.client.local.api;

/**
 * This exception is thrown when you try to navigate to a page that is not found in the Navigation graph.
 * 
 * @author Divya Dadlani <ddadlani@redhat.com>
 * 
 */
public class PageNotFoundException extends RuntimeException {

  public PageNotFoundException(String message) {
    super(message);
  }
}
