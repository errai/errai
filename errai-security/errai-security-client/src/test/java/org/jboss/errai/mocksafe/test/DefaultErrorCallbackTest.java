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

package org.jboss.errai.mocksafe.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
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
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.MissingPageRoleException;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.gwt.http.client.Request;
import com.google.gwtmockito.GwtMockitoTestRunner;

@RunWith(GwtMockitoTestRunner.class)
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

    verify(securityContext).redirectToLoginPage();
  }

  @Test
  public void testBusAuthorizationErrorNavigatesToSecurityErrorPage() throws Exception {
    busErrorCallback.handleError(new UnauthorizedException());

    verify(securityContext).redirectToSecurityErrorPage();
  }

  @Test
  public void testBusOtherErrorIsIgnored() throws Exception {
    busErrorCallback.handleError(new Exception());

    verifyZeroInteractions(securityContext);
  }

  @Test
  public void testBusErrorCallbackThrowsErrorWhenNoLoginPageExists() throws Exception {
    final MissingPageRoleException toBeThrown = new MissingPageRoleException(LoginPage.class);
    doThrow(toBeThrown).when(securityContext).redirectToLoginPage();

    try {
      busErrorCallback.handleError(new UnauthenticatedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).redirectToLoginPage();

      assertSame(toBeThrown, ex.getCause());
    }
  }

  @Test
  public void testBusErrorCallbackThrowsErrorWhenNoSecurityErrorPageExists() throws Exception {
    final MissingPageRoleException toBeThrown = new MissingPageRoleException(SecurityError.class);
    doThrow(toBeThrown).when(securityContext).redirectToSecurityErrorPage();

    try {
      busErrorCallback.handleError(new UnauthorizedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).redirectToSecurityErrorPage();

      assertSame(toBeThrown, ex.getCause());
    }
  }

  @Test
  public void testRestAuthenticationErrorNavigatesToLoginPage() throws Exception {
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    final boolean retVal = restErrorCallback.error(mock(Request.class), new UnauthenticatedException());

    assertFalse(retVal);
    verify(securityContext).redirectToLoginPage();
  }

  @Test
  public void testRestAuthorizationErrorNavigatesToSecurityErrorPage() throws Exception {
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    final boolean retVal = restErrorCallback.error(mock(Request.class), new UnauthorizedException());

    assertFalse(retVal);
    verify(securityContext).redirectToSecurityErrorPage();
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

    doThrow(toBeThrown).when(securityContext).redirectToSecurityErrorPage();
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    try {
      restErrorCallback.error(mock(Request.class), new UnauthorizedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).redirectToSecurityErrorPage();

      assertSame(toBeThrown, ex.getCause());
    }
  }

  @Test
  public void testRestErrorCallbackThrowsErrorWhenNoLoginPageExists() throws Exception {
    final MissingPageRoleException toBeThrown = new MissingPageRoleException(LoginPage.class);

    doThrow(toBeThrown).when(securityContext).redirectToLoginPage();
    when(wrapped.error(any(Request.class), any(Throwable.class))).thenReturn(true);

    try {
      restErrorCallback.error(mock(Request.class), new UnauthenticatedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).redirectToLoginPage();

      assertSame(toBeThrown, ex.getCause());
    }
  }

}
