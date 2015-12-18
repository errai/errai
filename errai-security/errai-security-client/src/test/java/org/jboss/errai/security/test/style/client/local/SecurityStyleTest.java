/*
 * Copyright (C) 2014 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.security.test.style.client.local;

import static org.jboss.errai.enterprise.client.cdi.api.CDI.*;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.client.local.context.SecurityContextImpl;
import org.jboss.errai.security.shared.api.Role;
import org.jboss.errai.security.shared.api.RoleImpl;
import org.jboss.errai.security.shared.api.annotation.RestrictedAccess;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.api.identity.UserImpl;
import org.jboss.errai.security.test.style.client.local.res.TemplatedStyleWidget;
import org.jboss.errai.ui.shared.api.style.StyleBindingsRegistry;
import org.junit.Test;

import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Widget;

public class SecurityStyleTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.test.style.StyleTest";
  }

  private final User regularUser;
  private final User adminUser;

  private final RoleImpl userRole = new RoleImpl("user");
  private final RoleImpl adminRole = new RoleImpl("admin");

  private SyncBeanManager bm;
  private SecurityContext securityContext;

  private TemplatedStyleWidget testWidget;

  public SecurityStyleTest() {
    final Set<Role> regularUserRoles = new HashSet<Role>();
    regularUserRoles.add(userRole);
    regularUser = new UserImpl("testuser", regularUserRoles);

    final Set<Role> adminUserRoles = new HashSet<Role>();
    adminUserRoles.add(userRole);
    adminUserRoles.add(adminRole);
    adminUser = new UserImpl("testadmin", adminUserRoles);

  }

  @Override
  protected void gwtSetUp() throws Exception {
    StyleBindingsRegistry.reset();
    super.gwtSetUp();
    bm = IOC.getBeanManager();
    securityContext = bm.lookupBean(SecurityContextImpl.class).getInstance();
    testWidget = bm.lookupBean(TemplatedStyleWidget.class).getInstance();
  }

  /**
   * Regression test for ERRAI-644.
   */
  @Test
  public void testTemplatedElementsStyleWhenNotLoggedIn() throws Exception {
    asyncTest();
    addPostInitTask(new Runnable() {

      @Override
      public void run() {
        // Make sure we are not logged in as anyone.
        securityContext.setCachedUser(User.ANONYMOUS);

        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getControl()));
        assertTrue(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getAuthenticatedAnchor()));
        assertTrue(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getUserAnchor()));
        assertTrue(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getUserAdminAnchor()));
        assertTrue(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getAdminAnchor()));

        finishTest();
      }
    });
  }

  @Test
  public void testTemplatedElementsStyleWithSomeRoles() throws Exception {
    asyncTest();
    addPostInitTask(new Runnable() {

      @Override
      public void run() {
        securityContext.setCachedUser(regularUser);

        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getControl()));
        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getAuthenticatedAnchor()));
        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getUserAnchor()));
        assertTrue(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getUserAdminAnchor()));
        assertTrue(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getAdminAnchor()));

        finishTest();
      }
    });
  }

  @Test
  public void testTemplatedElementsStyleFullyAuthorized() throws Exception {
    asyncTest();
    addPostInitTask(new Runnable() {

      @Override
      public void run() {
        securityContext.setCachedUser(adminUser);

        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getControl()));
        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getAuthenticatedAnchor()));
        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getUserAnchor()));
        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getUserAdminAnchor()));
        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getAdminAnchor()));

        finishTest();
      }
    });
  }

  @Test
  public void testAdditionalStyleBindingApplied() throws Exception {
    asyncTest();
    addPostInitTask(new Runnable() {

      @Override
      public void run() {
        // Make sure we are not logged in as anyone.
        securityContext.setCachedUser(User.ANONYMOUS);

        Anchor customStyledUserAnchor = testWidget.getCustomStyledUserAnchor();
        String color = customStyledUserAnchor.getElement().getStyle().getColor();
        String bgColor = customStyledUserAnchor.getElement().getStyle().getBackgroundColor();

        assertEquals("Custom style binding not applied", "red", color);
        assertEquals("Custom style binding not applied", "blue", bgColor);
        assertTrue(hasStyle(RestrictedAccess.CSS_CLASS_NAME, customStyledUserAnchor));

        securityContext.setCachedUser(adminUser);
        assertEquals("Custom style binding not applied", "red", color);
        assertEquals("Custom style binding not applied", "blue", bgColor);
        assertFalse(hasStyle(RestrictedAccess.CSS_CLASS_NAME, customStyledUserAnchor));

        finishTest();
      }
    });
  }

  public void testStyleBindingAppliedCorrectlyWithProvidedRoles() throws Exception {
    asyncTest();
    addPostInitTask(new Runnable() {

      @Override
      public void run() {
        securityContext.setCachedUser(regularUser);
        assertTrue(hasStyle(RestrictedAccess.CSS_CLASS_NAME, testWidget.getAnchorWithProvidedRoles()));
        finishTest();
      }
    });
  }

  private boolean hasStyle(final String name, final Widget widget) {
    String cssClasses = widget.getElement().getAttribute("class");
    return cssClasses != null && cssClasses.contains(name);
  }
}
