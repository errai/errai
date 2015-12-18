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

package org.jboss.errai.security.keycloak;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.security.server.mock.MockFilterConfig;
import org.jboss.errai.security.server.mock.MockServletContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.keycloak.KeycloakSecurityContext;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class LoginRedirectFilterTest {

  private static final String TEST_CONTEXT = "/test-context";

  @InjectMocks
  private LoginRedirectFilter loginRedirectFilter;

  @Mock
  private KeycloakAuthenticationService mockAuthService;

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private HttpServletResponse mockResponse;

  @Mock
  private FilterChain mockChain;

  private KeycloakSecurityContext securityContext;

  private MockFilterConfig mockFilterConfig;

  @Before
  public void setup() {
    final MockServletContext mockServletContext = new MockServletContext();
    mockServletContext.setContextPath(TEST_CONTEXT);

    final MockFilterConfig mockFilterConfig = new MockFilterConfig(mockServletContext);

    this.securityContext = new KeycloakSecurityContext();
    this.mockFilterConfig = mockFilterConfig;

    when(mockRequest.getAttribute(KeycloakSecurityContext.class.getName())).thenReturn(securityContext);
  }

  @Test
  public void redirectsToServletContextWithNoInitParam() throws Exception {
    loginRedirectFilter.init(mockFilterConfig);
    loginRedirectFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockRequest).getAttribute(KeycloakSecurityContext.class.getName());
    verify(mockAuthService).setSecurityContext(securityContext);
    verify(mockResponse).sendRedirect(TEST_CONTEXT);

    verifyNoMoreInteractions(mockRequest, mockResponse, mockChain, mockAuthService);
  }

  @Test
  public void redirectsToGivenContextPathWithInitParam() throws Exception {
    final String testPath = "/some-test-path";
    mockFilterConfig.initParams.put(LoginRedirectFilter.REDIRECT_PARAM_NAME, testPath);

    loginRedirectFilter.init(mockFilterConfig);
    loginRedirectFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockRequest).getAttribute(KeycloakSecurityContext.class.getName());
    verify(mockAuthService).setSecurityContext(securityContext);
    verify(mockResponse).sendRedirect(TEST_CONTEXT + testPath);

    verifyNoMoreInteractions(mockRequest, mockResponse, mockChain, mockAuthService);
  }

}
