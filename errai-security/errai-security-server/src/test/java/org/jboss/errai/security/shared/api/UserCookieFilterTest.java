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

  private void setUserCookieKeyInPropertiesMock(final Properties properties, final boolean value) {
    when(properties.containsKey(USER_COOKIE_ENABLED)).thenReturn(true);
    when(properties.get(USER_COOKIE_ENABLED)).thenReturn(value);
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
    setUserCookieKeyInPropertiesMock(properties, false);

    userFilter.init(mockConfig);
    userFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockChain).doFilter(mockRequest, mockResponse);
    verifyNoMoreInteractions(mockRequest, mockResponse, mockChain, mockAuthService);
  }

  @Test
  public void setCookieIfPropertySetToTrue() throws Exception {
    setUserCookieKeyInPropertiesMock(properties, true);

    userFilter.init(mockConfig);
    userFilter.doFilter(mockRequest, mockResponse, mockChain);

    verify(mockAuthService).getUser();
    verify(mockResponse).addCookie(Mockito.any(Cookie.class));
    verify(mockChain).doFilter(mockRequest, mockResponse);
    verifyNoMoreInteractions(mockRequest, mockResponse, mockChain, mockAuthService);
  }

}
