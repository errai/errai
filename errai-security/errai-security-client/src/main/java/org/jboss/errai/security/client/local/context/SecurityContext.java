package org.jboss.errai.security.client.local.context;

import org.jboss.errai.ui.nav.client.local.UniquePageRole;

public interface SecurityContext extends ActiveUserCache {

  /**
   * Navigate to the page with a given role, caching the current page so that it
   * may be revisited later.
   * 
   * @see {@link #getLastCachedPageName()}
   * @param roleClass
   *          The role of the page to navigate to.
   * @param lastPage
   *          The name of the last page to cache.
   */
  public void navigateToPage(Class<? extends UniquePageRole> roleClass, String lastPage);

  /**
   * Navigate to the page with a given role.
   * 
   * @param roleClass
   *          The role of the page to navigate to.
   */
  public void navigateToPage(Class<? extends UniquePageRole> roleClass);

  /**
   * Navigate to the last page a user was redirected from (via this security
   * context), or home if the user has not been redirected.
   */
  public void navigateBackOrHome();

  /**
   * @return The name of the last cached page.
   * @see {@link #navigateToPage(Class, String)}
   */
  public String getLastCachedPageName();

}