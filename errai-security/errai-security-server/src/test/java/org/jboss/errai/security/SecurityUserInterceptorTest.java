package org.jboss.errai.security;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.interceptor.InvocationContext;

import org.jboss.errai.security.server.SecurityUserInterceptor;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.junit.Test;

/**
 * @author edewit@redhat.com
 */
public class SecurityUserInterceptorTest {

  @Test
  public void shouldValidateIfUserIsLoggedIn() throws Exception {
    // given
    AuthenticationService authenticationService = mock(AuthenticationService.class);
    SecurityUserInterceptor interceptor = new SecurityUserInterceptor(authenticationService);
    InvocationContext context = mock(InvocationContext.class);


    // when
    when(authenticationService.isLoggedIn()).thenReturn(true);
    interceptor.aroundInvoke(context);

    // then
    verify(context).proceed();
  }
}
