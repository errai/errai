package org.jboss.errai.security.client.local.nav;

import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;

/**
 * A convenient way of returning to the previous page after being redirected to
 * the {@link LoginPage}.
 * 
 * @author Max Barkley <mbarkley@redhat.com>
 */
public interface PageReturn {

  /**
   * This is meant to be called from the login page. If the user has been
   * redirected to the login page by a {@link SecurityException} then return
   * them to the previous page. Otherwise go to the page with the
   * {@link DefaultPage} role.
   */
  public void goBackOrHome();

}
