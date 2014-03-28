/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.test.page.client.local;

import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Default;

import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.client.local.TestLoginPage;
import org.jboss.errai.security.client.local.TestPage;
import org.jboss.errai.security.client.local.TestSecurityErrorPage;
import org.jboss.errai.security.client.local.spi.ActiveUserCache;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.test.page.client.res.RequireAuthenticationPage;
import org.jboss.errai.security.test.page.client.res.RequiresRoleBasedPage;
import org.jboss.errai.ui.nav.client.local.DefaultPage;
import org.jboss.errai.ui.nav.client.local.Navigation;
import org.junit.Test;

import com.google.gwt.user.client.Timer;

public class SecureNavigationIntegrationTest extends AbstractErraiCDITest {

  private SyncBeanManager bm;
  private ActiveUserCache activeUserCache;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.test.page.PageTest";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    bm = IOC.getBeanManager();
    activeUserCache = bm.lookupBean(ActiveUserCache.class, new Annotation() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Default.class;
      }
    }).getInstance();

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
        final User user = new UserImpl("testuser");
        // Cache a logged in user.
        activeUserCache.setUser(user);

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
        final Set<Role> roles = new HashSet<Role>();
        roles.add(new RoleImpl("user"));
        final User user = new UserImpl("testuser", roles);

        // Cache a logged in user.
        activeUserCache.setUser(user);

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
        final Set<Role> roles = new HashSet<Role>();
        roles.add(new RoleImpl("user"));
        roles.add(new RoleImpl("admin"));
        final User user = new UserImpl("testuser", roles);

        // Cache a logged in user.
        activeUserCache.setUser(user);

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
