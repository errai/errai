package org.jboss.errai.security.client.local.nav;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class SecurityNavigationUtil {

  private static String lastPageCache;

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
  public static void navigateToPage(final Class<? extends UniquePageRole> roleClass, final String lastPage) {
    IOC.getAsyncBeanManager().lookupBean(Navigation.class).getInstance(new CreationalCallback<Navigation>() {

      @Override
      public void callback(final Navigation navigation) {
        final String pageName;
        if (lastPage != null) {
          pageName = lastPage;
        }
        // Edge case: first page load of app.
        else if (navigation.getCurrentPage() != null) {
          pageName = navigation.getCurrentPage().name();
        }
        else {
          pageName = null;
        }

        if (pageName != null) {
          lastPageCache = pageName;
        }
        navigation.goToWithRole(roleClass);
      }
    });
  }

  /**
   * Navigate to the page with a given role.
   * 
   * @param roleClass The role of the page to navigate to.
   */
  public static void navigateToPage(final Class<? extends UniquePageRole> roleClass) {
    navigateToPage(roleClass, null);
  }

  /**
   * @return The name of the last cached page.
   * @see {@link #navigateToPage(Class, String)}
   */
  public static String getLastCachedPageName() {
    return lastPageCache;
  }

}
