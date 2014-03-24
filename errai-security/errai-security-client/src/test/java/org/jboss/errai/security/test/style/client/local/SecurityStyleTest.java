package org.jboss.errai.security.test.style.client.local;

import static org.jboss.errai.enterprise.client.cdi.api.CDI.addPostInitTask;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.SyncBeanManager;
import org.jboss.errai.security.client.local.context.SecurityContext;
import org.jboss.errai.security.client.local.context.impl.SecurityContextImpl;
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.test.style.client.local.res.TemplatedStyleWidget;
import org.junit.Test;

public class SecurityStyleTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.security.test.style.StyleTest";
  }
  
  private final User regularUser;
  private final User adminUser;
  
  private final Role userRole = new Role("user");
  private final Role adminRole = new Role("admin");
  
  private SyncBeanManager bm;
  private SecurityContext securityContext;
  
  public SecurityStyleTest() {
    regularUser = new User();
    final Set<Role> regularUserRoles = new HashSet<Role>();
    regularUserRoles.add(userRole);
    regularUser.setRoles(regularUserRoles);

    adminUser = new User();
    final Set<Role> adminUserRoles = new HashSet<Role>();
    adminUserRoles.add(userRole);
    adminUserRoles.add(adminRole);
    adminUser.setRoles(adminUserRoles);
  }
  
  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
    addPostInitTask(new Runnable() {
      
      @Override
      public void run() {
        bm = IOC.getBeanManager();
        securityContext = bm.lookupBean(SecurityContextImpl.class).getInstance();
      }
    });
  }
  
  /**
   * Regression test for ERRAI-644.
   */
  @Test
  public void testTemplatedElementsHiddenWhenNotLoggedIn() throws Exception {
    asyncTest();
    addPostInitTask(new Runnable() {
      
      @Override
      public void run() {
        final TemplatedStyleWidget widget = bm.lookupBean(TemplatedStyleWidget.class).getInstance();
        // Make sure we are not logged in as anyone.
        securityContext.getActiveUserCache().setUser(null);
        
        assertTrue(widget.getControl().isVisible());
        assertFalse(widget.getUserAnchor().isVisible());
        assertFalse(widget.getUserAdminAnchor().isVisible());
        assertFalse(widget.getAdminAnchor().isVisible());

        finishTest();
      }
    });
  }
  
  @Test
  public void testTemplatedElementsHiddenWhenUnauthorized() throws Exception {
    asyncTest();
    addPostInitTask(new Runnable() {
      
      @Override
      public void run() {
        final TemplatedStyleWidget widget = bm.lookupBean(TemplatedStyleWidget.class).getInstance();

        securityContext.getActiveUserCache().setUser(regularUser);
        
        assertTrue(widget.getControl().isVisible());
        assertTrue(widget.getUserAnchor().isVisible());
        assertFalse(widget.getUserAdminAnchor().isVisible());
        assertFalse(widget.getAdminAnchor().isVisible());

        finishTest();
      }
    });
  }
  
  @Test
  public void testTemplatedElementsShownWhenAuthorized() throws Exception {
    asyncTest();
    addPostInitTask(new Runnable() {
      
      @Override
      public void run() {
        final TemplatedStyleWidget widget = bm.lookupBean(TemplatedStyleWidget.class).getInstance();

        securityContext.getActiveUserCache().setUser(adminUser);
        
        assertTrue(widget.getControl().isVisible());
        assertTrue(widget.getUserAnchor().isVisible());
        assertTrue(widget.getUserAdminAnchor().isVisible());
        assertTrue(widget.getAdminAnchor().isVisible());

        finishTest();
      }
    });
  }

}
