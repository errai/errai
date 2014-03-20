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
import org.jboss.errai.security.shared.api.identity.Role;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.shared.service.AuthenticationService;
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
    final User user = new User();
    user.setRoles(Arrays.asList(new Role("admin"), new Role("user")));
    when(authenticationService.getUser()).thenReturn(user);
    interceptor.aroundInvoke(context);

    // then
    verify(context).proceed();
  }

  @Test(expected = UnauthorizedException.class)
  public void shouldThrowExceptionWhenUserNotInRole() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    invokeTest(context, new Service());

    // then
    fail("security exception should have been thrown");
  }

  @Test(expected = UnauthenticatedException.class)
  public void shouldThrowExceptionWhenNotLoggedIn() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    when(context.getTarget()).thenReturn(new Service());
    when(context.getMethod()).thenReturn(getAnnotatedServiceMethod());
    when(authenticationService.getUser()).thenReturn(null);
    interceptor.aroundInvoke(context);

    fail("exception shoudl have been thrown");
  }

  @Test(expected = UnauthorizedException.class)
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
    final User user = new User();
    user.setRoles(new ArrayList<Role>());
    when(authenticationService.getUser()).thenReturn(user);
    interceptor.aroundInvoke(context);
  }

  @Test(expected = UnauthorizedException.class)
  public void shouldFindMethodWhenOnInterface() throws Exception {
    // given
    InvocationContext context = mock(InvocationContext.class);

    // when
    when(context.getTarget()).thenReturn(new Service());
    when(context.getMethod()).thenReturn(Service.class.getMethod("annotatedServiceMethod"));
    final User user = new User();
    user.setRoles(new ArrayList<Role>());
    when(authenticationService.getUser()).thenReturn(user);
    interceptor.aroundInvoke(context);

    // then
    fail("security exception should have been thrown");
  }

  private Method getAnnotatedServiceMethod() throws NoSuchMethodException {
    return ServiceInterface.class.getMethod("annotatedServiceMethod");
  }
}
