package org.jboss.errai.security.client.local.util;

import java.util.LinkedList;
import java.util.Queue;

import javax.annotation.PostConstruct;
import javax.enterprise.event.Event;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.jboss.errai.ioc.client.api.EntryPoint;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.event.LoggedInEvent;
import org.jboss.errai.security.shared.event.LoggedOutEvent;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.jboss.errai.ui.nav.client.local.UniquePageRole;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@EntryPoint
public class SecurityUtil {

  @Singleton
  public static class SecurityModule {

    @Inject
    private Event<LoggedInEvent> loginEvent;

    @Inject
    private Event<LoggedOutEvent> logoutEvent;

    @Inject
    private ActiveUserProvider userProvider;

    @Inject
    private Navigation nav;

  }

  public static interface ModuleRunnable {
    public void run(SecurityModule module);
  }

  private static String lastPageCache;
  private static SecurityModule moduleInstance;
  private static Queue<ModuleRunnable> runnables = new LinkedList<ModuleRunnable>();

  @Inject
  private SecurityModule module;

  @PostConstruct
  private void init() {
    moduleInstance = module;
    while (!runnables.isEmpty()) {
      runnables.poll().run(moduleInstance);
    }
  }

  private static void runWithModule(final ModuleRunnable runnable) {
    if (moduleInstance != null) {
      runnable.run(moduleInstance);
    }
    else {
      runnables.offer(runnable);
    }
  }

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
    runWithModule(new ModuleRunnable() {

      @Override
      public void run(final SecurityModule module) {
        final String pageName;
        if (lastPage != null) {
          pageName = lastPage;
        }
        // Edge case: first page load of app.
        else if (module.nav.getCurrentPage() != null) {
          pageName = module.nav.getCurrentPage().name();
        }
        else {
          pageName = null;
        }

        if (pageName != null) {
          lastPageCache = pageName;
        }
        module.nav.goToWithRole(roleClass);
      }
    });
  }

  /**
   * Navigate to the page with a given role.
   * 
   * @param roleClass
   *          The role of the page to navigate to.
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

  public static void invalidateUserCache() {
    runWithModule(new ModuleRunnable() {

      @Override
      public void run(final SecurityModule module) {
        if (module.userProvider.isCacheValid()) {
          // User must be updated before style bindings updated.
          module.userProvider.invalidateCache();
          StyleBindingsRegistry.get().updateStyles();
          module.logoutEvent.fire(new LoggedOutEvent());
        }
      }
    });
  }

  public static void performLoginStatusChangeActions(final User user) {
    runWithModule(new ModuleRunnable() {

      @Override
      public void run(final SecurityModule module) {
        if ((user != null && !user.equals(module.userProvider.getActiveUser()))
                || (user == null && module.userProvider.hasActiveUser())) {
          // User must be updated before style bindings updated.
          module.userProvider.setActiveUser(user);
          StyleBindingsRegistry.get().updateStyles();
          if (user == null) {
            module.logoutEvent.fire(new LoggedOutEvent());
          }
          else {
            module.loginEvent.fire(new LoggedInEvent(user));
          }
        }
      }
    });
  }

}
