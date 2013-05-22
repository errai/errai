package org.jboss.errai.security;

import org.jboss.errai.common.client.framework.ProxyProvider;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.security.server.SecurityUserInterceptor;
import org.jboss.errai.security.shared.SecurityManager;
import org.junit.Test;

import javax.interceptor.InvocationContext;

import static org.jgroups.util.Util.assertTrue;
import static org.mockito.Mockito.*;

/**
 * @author edewit@redhat.com
 */
public class SecurityUserInterceptorTest {

  @Test
  public void shouldValidateIfUserIsLoggedIn() throws Exception {
    // given
    SecurityManager securityManager = mock(SecurityManager.class);
    SecurityUserInterceptor interceptor = new SecurityUserInterceptor(securityManager);
    InvocationContext context = mock(InvocationContext.class);


    // when
    when(securityManager.isLoggedIn()).thenReturn(true);
    interceptor.aroundInvoke(context);

    // then
    verify(context).proceed();
  }

  @Test
  public void shouldValidateIfUserIsLoggedInClientSide() {
    //given
    final Boolean[] redirectToLoginPage = {Boolean.FALSE};
    org.jboss.errai.security.client.local.SecurityUserInterceptor interceptor
            = new org.jboss.errai.security.client.local.SecurityUserInterceptor() {
      @Override
      protected void navigateToLoginPage() {
        redirectToLoginPage[0] = Boolean.TRUE;
      }
    };
    RemoteServiceProxyFactory.addRemoteProxy(SecurityManager.class, new ProxyProvider() {
      @Override
      public Object getProxy() {
        return new SecurityRoleInterceptorTest.MockSecurityManager();
      }
    });


    //when
    interceptor.aroundInvoke(null);

    //then
    assertTrue(redirectToLoginPage[0]);
  }
}
