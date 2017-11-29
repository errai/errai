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

import static org.junit.Assert.assertSame;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.*;

import org.jboss.errai.enterprise.client.jaxrs.api.RestErrorCallback;
import org.jboss.errai.security.client.local.api.SecurityContext;
import org.jboss.errai.security.client.local.callback.DefaultBusSecurityErrorCallback;
import org.jboss.errai.security.client.local.callback.DefaultRestSecurityErrorCallback;
import org.jboss.errai.security.client.local.handler.DefaultSecurityExceptionHandler;
import org.jboss.errai.security.shared.exception.SecurityException;
import org.jboss.errai.security.shared.exception.UnauthenticatedException;
import org.jboss.errai.security.shared.exception.UnauthorizedException;
import org.jboss.errai.ui.nav.client.local.api.LoginPage;
import org.jboss.errai.ui.nav.client.local.api.MissingPageRoleException;
import org.jboss.errai.ui.nav.client.local.api.SecurityError;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import com.google.gwt.http.client.Request;
import com.google.gwtmockito.GwtMockitoTestRunner;
import org.mockito.Spy;

@RunWith(GwtMockitoTestRunner.class)
public class DefaultErrorCallbackTest {

  @Mock(name = "context")
  private SecurityContext securityContext;

  @Mock(name = "wrapped")
  private RestErrorCallback wrapped;

  @Spy @InjectMocks
  private DefaultSecurityExceptionHandler handler;

  private DefaultBusSecurityErrorCallback busErrorCallback;
  private DefaultRestSecurityErrorCallback restErrorCallback;

  @Before
  public void setup() {
    busErrorCallback = new DefaultBusSecurityErrorCallback(handler);
    restErrorCallback = new DefaultRestSecurityErrorCallback(handler);
  }

  @Test
  public void testBusAuthenticationErrorRedirectsToLoginPage() throws Exception {
    handler.handleException(new UnauthenticatedException());

    verify(securityContext).redirectToLoginPage();
  }

  @Test
  public void testBusAuthorizationErrorRedirectsToSecurityErrorPage() throws Exception {
    handler.handleException(new UnauthorizedException());

    verify(securityContext).redirectToSecurityErrorPage();
  }

  @Test
  public void testBusOtherErrorIsIgnored() throws Exception {
    handler.handleException(new Exception());

    verifyZeroInteractions(securityContext);
  }

  @Test
  public void testBusErrorCallbackThrowsErrorWhenNoLoginPageExists() throws Exception {
    final MissingPageRoleException toBeThrown = new MissingPageRoleException(LoginPage.class);
    doThrow(toBeThrown).when(securityContext).redirectToLoginPage();

    try {
      handler.handleException(new UnauthenticatedException());
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
      handler.handleException(new UnauthorizedException());
      fail("No exception was thrown.");
    }
    catch (RuntimeException ex) {
      // Precondition
      verify(securityContext).redirectToSecurityErrorPage();

      assertSame(toBeThrown, ex.getCause());
    }
  }

  @Test
  public void testBusCallingHandlerWhenSecurityException() throws Exception {
    SecurityException toBeThrown = new UnauthenticatedException();

    busErrorCallback.handleError(toBeThrown);

    verify(handler).handleException(toBeThrown);
  }

  @Test
  public void testRestCallingHandlerWhenSecurityException() throws Exception {
    SecurityException toBeThrown = new UnauthenticatedException();

    restErrorCallback.error(mock(Request.class), toBeThrown);

    verify(handler).handleException(toBeThrown);
  }

}
