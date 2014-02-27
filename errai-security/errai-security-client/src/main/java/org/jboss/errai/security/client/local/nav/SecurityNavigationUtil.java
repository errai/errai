package org.jboss.errai.security.client.local.nav;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;

public class SecurityNavigationUtil {
  
  private static String lastPageCache;
  
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
  
  public static void navigateToPage(final Class<? extends UniquePageRole> roleClass) {
    navigateToPage(roleClass, null);
  }
  
  public static String getLastCachedPageName() {
    return lastPageCache;
  }

}
