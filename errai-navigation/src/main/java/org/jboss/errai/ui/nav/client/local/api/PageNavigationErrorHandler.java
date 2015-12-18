package org.jboss.errai.ui.nav.client.local.api;

import org.jboss.errai.ui.nav.client.local.UniquePageRole;

/**
 * Defines an error handler used for page navigation errors.
 * 
 * @author Divya Dadlani <ddadlani@redhat.com>
 */
public interface PageNavigationErrorHandler {

  /**
   * @param exception
   *          The exception that occurs, triggering the error handler code.
   * @param pageName
   *          The name of the page which we tried to navigate to.
   */
  public void handleInvalidPageNameError(Exception exception, String pageName);

  /**
   * @param exception
   *          The exception that occurs, triggering the error handler code.
   * @param pageRole
   *          The role of the page which we tried to navigate to.
   */
  public void handleError(Exception exception, Class<? extends UniquePageRole> pageRole);

  /**
   * 
   * @param exception
   *          The exception that occurs, triggering the error handler code.
   * @param urlPath
   *          The URL path which we tried to navigate to.
   */
  public void handleInvalidURLError(Exception exception, String urlPath);
}
