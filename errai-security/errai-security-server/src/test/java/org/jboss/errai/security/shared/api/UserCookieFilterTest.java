/**
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.errai.security.shared.api;

import static org.jboss.errai.security.Properties.USER_COOKIE_ENABLED;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.util.Properties;

import javax.servlet.FilterChain;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.security.server.mock.MockFilterConfig;
import org.jboss.errai.security.server.servlet.UserCookieFilter;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
@RunWith(MockitoJUnitRunner.class)
public class UserCookieFilterTest {

  @InjectMocks
  private UserCookieFilter userFilter;

  @Mock
  private Properties properties;

  @Mock
  private HttpServletRequest mockRequest;

  @Mock
  private HttpServletResponse mockResponse;

  @Mock
  private FilterChain mockChain;

  @Mock
  private MockFilterConfig mockConfig;

  @Mock
  private AuthenticationService mockAuthService;

  private void setUserCookieKeyInPropertiesMock(final Properties properties, final String value) {
    when(properties.containsKey(USER_COOKIE_ENABLED)).thenReturn(true);
    when(properties.getProperty(USER_COOKIE_ENABLED)).thenReturn(value);
  }

  @Test
  public void doNotSetCookieIfNoProperty() throws Exception {
    userFilter.init(mockConfig);
    userFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockChain).doFilter(mockRequest, mockResponse);
    verifyNoMoreInteractions(mockRequest, mockResponse, mockChain, mockAuthService);
  }

  @Test
  public void doNotSetCookieIfPropertySetToFalse() throws Exception {
    setUserCookieKeyInPropertiesMock(properties, "false");

    userFilter.init(mockConfig);
    userFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockChain).doFilter(mockRequest, mockResponse);
    verifyNoMoreInteractions(mockRequest, mockResponse, mockChain, mockAuthService);
  }

  @Test
  public void setCookieIfPropertySetToTrue() throws Exception {
    setUserCookieKeyInPropertiesMock(properties, "true");

    userFilter.init(mockConfig);
    userFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockAuthService).getUser();
    verify(mockResponse).addCookie(Mockito.any(Cookie.class));
    verify(mockChain).doFilter(mockRequest, mockResponse);
    verifyNoMoreInteractions(mockRequest, mockResponse, mockChain, mockAuthService);
  }

}
