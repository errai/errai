package org.jboss.errai.security;

import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import javax.interceptor.InvocationContext;

import org.jboss.errai.security.client.shared.ServiceInterface;
import org.jboss.errai.security.res.Service;
import org.jboss.errai.security.server.ServerSecurityRoleInterceptor;
import org.jboss.errai.security.shared.AuthenticationService;
import org.jboss.errai.security.shared.Role;
import org.junit.Before;
import org.junit.Test;

/**
 * @author edewit@redhat.com
 */
public class SecurityRoleInterceptorTest {
  private AuthenticationService authenticationService;
  private ServerSecurityRoleInterceptor interceptor;

  @Before
  public void setUp() throws Exception {
    authenticationService = mock(AuthenticationService.class);
    interceptor = new ServerSecurityRoleInterceptor(authenticationService);
  }

  @Test
  public void shouldVerifyUserInRole() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    when(context.getTarget()).thenReturn(new Service());
    when(context.getMethod()).thenReturn(getAnnotatedServiceMethod());
    when(authenticationService.getRoles()).thenReturn(Arrays.asList(new Role("admin"), new Role("user")));
    interceptor.aroundInvoke(context);

    // then
    verify(context).proceed();
  }

  @Test(expected = SecurityException.class)
  public void shouldThrowExceptionWhenUserNotInRole() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    invokeTest(context, new Service());

    // then
    fail("security exception should have been thrown");
  }

  @Test(expected = SecurityException.class)
  public void shouldFindMethodWhenNoInterface() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    invokeTest(context, this);

    // then
    fail("security exception should have been thrown");
  }

  private void invokeTest(InvocationContext context, Object service) throws Exception {
    when(context.getTarget()).thenReturn(service);
    when(context.getMethod()).thenReturn(getAnnotatedServiceMethod());
    when(authenticationService.getRoles()).thenReturn(new ArrayList<Role>());
    interceptor.aroundInvoke(context);
  }

  @Test(expected = SecurityException.class)
  public void shouldFindMethodWhenOnInterface() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    when(context.getTarget()).thenReturn(new Service());
    when(context.getMethod()).thenReturn(Service.class.getMethod("annotatedServiceMethod"));
    when(authenticationService.getRoles()).thenReturn(new ArrayList<Role>());
    interceptor.aroundInvoke(context);

    // then
    fail("security exception should have been thrown");
  }
//
//  @Test
//  public void shouldVerifyUserInRoleClientSide() throws Exception {
//    //given
//    RemoteCallContext context = mock(RemoteCallContext.class);
//    RemoteServiceProxyFactory.addRemoteProxy(AuthenticationService.class, new ProxyProvider() {
//      @Override
//      public Object getProxy() {
//        return new MockAuthenticationService(Arrays.asList(new Role("user")));
//      }
//    });
//
//    final Boolean[] redirectToLoginPage = {Boolean.FALSE};
//    final org.jboss.errai.security.client.local.ClientSecurityRoleInterceptor interceptor =
//            new org.jboss.errai.security.client.local.ClientSecurityRoleInterceptor() {
//      @Override
//      protected void navigateToPage(Class<? extends UniquePageRole> roleClass) {
//        redirectToLoginPage[0] = Boolean.TRUE;
//      }
//    };
//
//    //when
//    when(context.getAnnotations()).thenReturn(getAnnotatedServiceMethod().getAnnotations());
//    interceptor.aroundInvoke(context);
//
//    //then
//    assertTrue(redirectToLoginPage[0]);
//  }

  private Method getAnnotatedServiceMethod() throws NoSuchMethodException {
    return ServiceInterface.class.getMethod("annotatedServiceMethod");
  }
}
