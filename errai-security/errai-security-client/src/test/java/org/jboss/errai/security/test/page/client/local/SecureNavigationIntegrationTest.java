package org.jboss.errai.security.test.page.client.local;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.client.local.TestLoginPage;
import org.jboss.errai.security.client.local.TestPage;
import org.jboss.errai.security.client.local.TestSecurityErrorPage;
import org.jboss.errai.security.client.local.identity.ActiveUserProvider;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.test.page.client.res.RequireAuthenticationPage;
import org.jboss.errai.security.test.page.client.res.RequiresRoleBasedPage;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.junit.Test;

import com.google.gwt.user.client.Timer;

public class SecureNavigationIntegrationTest extends AbstractErraiCDITest {

  private SyncBeanManager bm;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.test.page.PageTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    bm = IOC.getBeanManager();
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      
      @Override
      public void run() {
        bm.lookupBean(Navigation.class).getInstance().goToWithRole(DefaultPage.class);
      }
    });
  }

  @Override
  protected void gwtTearDown() throws Exception {
    bm = null;
    super.gwtTearDown();
  }

  @Test
  public void testRequireAuthenticationNotLoggedIn() throws Exception {
    runNavTest(new Runnable() {

      @Override
      public void run() {
        final Navigation nav = bm.lookupBean(Navigation.class).getInstance();

        // Precondition
        assertEquals(TestPage.class, nav.getCurrentPage().contentType());

        nav.goTo("RequireAuthenticationPage");

        assertEquals(TestLoginPage.class, nav.getCurrentPage().contentType());
        finishTest();
      }
    });
  }

  @Test
  public void testRequireAuthenticationLoggedIn() throws Exception {
    runNavTest(new Runnable() {

      @Override
      public void run() {
        final User user = new User();
        // Cache a logged in user.
        bm.lookupBean(ActiveUserProvider.class).getInstance().setActiveUser(user);

        final Navigation nav = bm.lookupBean(Navigation.class).getInstance();

        // Precondition
        assertEquals(TestPage.class, nav.getCurrentPage().contentType());

        nav.goTo("RequireAuthenticationPage");

        assertEquals(RequireAuthenticationPage.class, nav.getCurrentPage().contentType());
        finishTest();
      }
    });
  }

  @Test
  public void testRequireRoleNotLoggedIn() throws Exception {
    runNavTest(new Runnable() {

      @Override
      public void run() {
        final Navigation nav = bm.lookupBean(Navigation.class).getInstance();

        // Precondition
        assertEquals(TestPage.class, nav.getCurrentPage().contentType());

        nav.goTo("RequiresRoleBasedPage");

        assertEquals(TestLoginPage.class, nav.getCurrentPage().contentType());
        finishTest();
      }
    });
  }

  @Test
  public void testRequireRoleUnauthorized() throws Exception {
    runNavTest(new Runnable() {

      @Override
      public void run() {
        final User user = new User();
        final Set<Role> roles = new HashSet<Role>();
        roles.add(new Role("user"));
        user.setRoles(roles);

        // Cache a logged in user.
        bm.lookupBean(ActiveUserProvider.class).getInstance().setActiveUser(user);

        final Navigation nav = bm.lookupBean(Navigation.class).getInstance();

        // Precondition
        assertEquals(TestPage.class, nav.getCurrentPage().contentType());

        nav.goTo("RequiresRoleBasedPage");

        assertEquals(TestSecurityErrorPage.class, nav.getCurrentPage().contentType());
        finishTest();
      }
    });
  }

  @Test
  public void testRequireRoleAuthorized() throws Exception {
    runNavTest(new Runnable() {

      @Override
      public void run() {
        final User user = new User();
        final Set<Role> roles = new HashSet<Role>();
        roles.add(new Role("user"));
        roles.add(new Role("admin"));

        user.setRoles(roles);
        // Cache a logged in user.
        bm.lookupBean(ActiveUserProvider.class).getInstance().setActiveUser(user);

        final Navigation nav = bm.lookupBean(Navigation.class).getInstance();

        // Precondition
        assertEquals(TestPage.class, nav.getCurrentPage().contentType());

        nav.goTo("RequiresRoleBasedPage");

        assertEquals(RequiresRoleBasedPage.class, nav.getCurrentPage().contentType());
        finishTest();
      }
    });
  }

  private void runNavTest(final Runnable runnable) {
    asyncTest();
    InitVotes.registerOneTimeInitCallback(new Runnable() {

      @Override
      public void run() {
        /*
         * This timer is here so that any failed assertions are thrown somewhere
         * where the test runner can catch them.
         */
        new Timer() {

          @Override
          public void run() {
            runnable.run();
          }
        }.schedule(100);
        ;
      }
    });
  }

}
