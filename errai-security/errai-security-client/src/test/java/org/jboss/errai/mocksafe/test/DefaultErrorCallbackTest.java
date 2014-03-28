package org.jboss.errai.mocksafe.test;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.client.local.callback.DefaultBusSecurityErrorCallback;
import org.jboss.errai.security.client.local.callback.DefaultRestSecurityErrorCallback;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.security.util.GwtMockitoRunnerExtension;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.MissingPageRoleException;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.gwt.http.client.Request;

@RunWith(GwtMockitoRunnerExtension.class)
public class DefaultErrorCallbackTest {

  @Mock
  private SecurityContext securityContext;

  @Mock(name = "wrapped")
  private RestErrorCallback wrapped;

  @InjectMocks
  private DefaultBusSecurityErrorCallback busErrorCallback;

  @InjectMocks
  private DefaultRestSecurityErrorCallback restErrorCallback;

  @Test
  public void testBusAuthenticationErrorNavigatesToLoginPage() throws Exception {
    busErrorCallback.handleError(new UnauthenticatedException());

    verify(securityContext).navigateToPage(LoginPage.class);
  }

  @Test
  public void testBusAuthorizationErrorNavigatesToSecurityErrorPage() throws Exception {
    busErrorCallback.handleError(new UnauthorizedException());

    verify(securityContext).navigateToPage(SecurityError.class);
  }

  @Test
  public void testBusOtherErrorIsIgnored() throws Exception {
    busErrorCallback.handleError(new Exception());

    verifyZeroInteractions(securityContext);
  }

  @Test
  public void testBusErrorCallbackThrowsErrorWhenNoLoginPageExists() throws Exception {
    final MissingPageRoleException toBeThrown = new MissingPageRoleException(LoginPage.class);
    doThrow(toBeThrown).when(securityContext).navigateToPage(LoginPage.class);

    try {
      busErrorCallback.handleError(new UnauthenticatedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).navigateToPage(LoginPage.class);

      assertSame(toBeThrown, ex.getCause());
    }
  }

  @Test
  public void testBusErrorCallbackThrowsErrorWhenNoSecurityErrorPageExists() throws Exception {
    final MissingPageRoleException toBeThrown = new MissingPageRoleException(SecurityError.class);
    doThrow(toBeThrown).when(securityContext).navigateToPage(SecurityError.class);

    try {
      busErrorCallback.handleError(new UnauthorizedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).navigateToPage(SecurityError.class);

      assertSame(toBeThrown, ex.getCause());
    }
  }

  @Test
  public void testRestAuthenticationErrorNavigatesToLoginPage() throws Exception {
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    final boolean retVal = restErrorCallback.error(mock(Request.class), new UnauthenticatedException());

    assertFalse(retVal);
    verify(securityContext).navigateToPage(LoginPage.class);
  }

  @Test
  public void testRestAuthorizationErrorNavigatesToSecurityErrorPage() throws Exception {
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    final boolean retVal = restErrorCallback.error(mock(Request.class), new UnauthorizedException());

    assertFalse(retVal);
    verify(securityContext).navigateToPage(SecurityError.class);
  }

  @Test
  public void testRestOtherErrorIsIgnored() throws Exception {
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    final boolean retVal = restErrorCallback.error(mock(Request.class), new Throwable());

    assertTrue(retVal);
    verifyNoMoreInteractions(securityContext);
  }

  @Test
  public void testRestErrorCallbackThrowsErrorWhenNoSecurityErrorPageExists() throws Exception {
    final MissingPageRoleException toBeThrown = new MissingPageRoleException(SecurityError.class);

    doThrow(toBeThrown).when(securityContext).navigateToPage(SecurityError.class);
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    try {
      restErrorCallback.error(mock(Request.class), new UnauthorizedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).navigateToPage(SecurityError.class);

      assertSame(toBeThrown, ex.getCause());
    }
  }

  @Test
  public void testRestErrorCallbackThrowsErrorWhenNoLoginPageExists() throws Exception {
    final MissingPageRoleException toBeThrown = new MissingPageRoleException(LoginPage.class);

    doThrow(toBeThrown).when(securityContext).navigateToPage(LoginPage.class);
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    try {
      restErrorCallback.error(mock(Request.class), new UnauthenticatedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).navigateToPage(LoginPage.class);

      assertSame(toBeThrown, ex.getCause());
    }
  }

}
