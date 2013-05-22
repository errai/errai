package org.jboss.errai.security;

import org.jboss.errai.bus.client.framework.AbstractRpcProxy;
import org.jboss.errai.common.client.api.interceptor.RemoteCallContext;
import org.jboss.errai.common.client.framework.ProxyProvider;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.security.server.SecurityRoleInterceptor;
import org.jboss.errai.security.shared.SecurityManager;
import org.jboss.errai.security.shared.*;
import org.junit.Before;
import org.junit.Test;

import javax.interceptor.InvocationContext;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

/**
 * @author edewit@redhat.com
 */
public class SecurityRoleInterceptorTest {
  private SecurityManager securityManager;
  private SecurityRoleInterceptor interceptor;

  @Before
  public void setUp() throws Exception {
    securityManager = mock(SecurityManager.class);
    interceptor = new SecurityRoleInterceptor(securityManager);
  }

  @Test
  public void shouldVerifyUserInRole() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    when(context.getMethod()).thenReturn(getClass().getMethod("annotatedServiceMethod"));
    when(securityManager.getRoles()).thenReturn(Arrays.asList(new Role("admin"), new Role("user")));
    interceptor.aroundInvoke(context);

    // then
    verify(context).proceed();
  }

  @Test(expected = SecurityException.class)
  public void shouldThrowExceptionWhenUserNotInRole() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    when(context.getMethod()).thenReturn(getClass().getMethod("annotatedServiceMethod"));
    when(securityManager.getRoles()).thenReturn(new ArrayList<Role>());
    interceptor.aroundInvoke(context);

    // then
    fail("security exception should have been thrown");
  }

  @Test
  public void shouldVerifyUserInRoleClientSide() throws Exception {
    //given
    RemoteCallContext context = mock(RemoteCallContext.class);
    RemoteServiceProxyFactory.addRemoteProxy(SecurityManager.class, new ProxyProvider() {
      @Override
      public Object getProxy() {
        return new MockSecurityManager(Arrays.asList(new Role("user")));
      }
    });

    final Boolean[] redirectToLoginPage = {Boolean.FALSE};
    interceptor = new SecurityRoleInterceptor(securityManager) {
      @Override
      protected void navigateToLoginPage() {
        redirectToLoginPage[0] = Boolean.TRUE;
      }
    };

    //when
    when(context.getAnnotations()).thenReturn(getClass().getMethod("annotatedServiceMethod").getAnnotations());
    interceptor.aroundInvoke(context);

    //then
    assertTrue(redirectToLoginPage[0]);
  }


  @RequireRoles("admin")
  @SuppressWarnings("UnusedDeclaration")
  public void annotatedServiceMethod() {}

  @SuppressWarnings("unchecked")
  public static class MockSecurityManager extends AbstractRpcProxy implements SecurityManager {
    List<Role> roleList;

    public MockSecurityManager() {
    }

    public MockSecurityManager(List<Role> roleList) {
      this.roleList = roleList;
    }

    @Override
    public void login(String username, String password) {
    }

    @Override
    public boolean isLoggedIn() {
      remoteCallback.callback(Boolean.FALSE);
      return false;
    }

    @Override
    public void logout() {
    }

    @Override
    public User getUser() {
      return null;
    }

    @Override
    public List<Role> getRoles() {
      remoteCallback.callback(roleList);
      return roleList;
    }
  }
}
