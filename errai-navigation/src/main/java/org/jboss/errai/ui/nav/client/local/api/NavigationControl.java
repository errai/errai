package org.jboss.errai.ui.nav.client.local.api;

import org.jboss.errai.ui.nav.client.local.PageHiding;

/**
 * Instances of this class are passed to {@link PageHiding} methods. If the parameter is present,
 * the page navigation will not be carried out until {@link NavigationControl#proceed()} is invoked.
 * This is useful for interrupting page navigations and then resuming at a later time (for example,
 * to prompt the user to save their work before transitioning to a new page).
 * 
 */
public class NavigationControl {

  private final Runnable runnable;
  
  private boolean hasRun;

  public NavigationControl(final Runnable runnable) {
    this.runnable = runnable;
  }

  /**
   * Causes page navigation to proceed.
   */
  public void proceed() {
    if (!hasRun) {
      runnable.run();
      hasRun = true;
    }
    else {
      throw new IllegalStateException("This method can only be called once.");
    }
  }
}
