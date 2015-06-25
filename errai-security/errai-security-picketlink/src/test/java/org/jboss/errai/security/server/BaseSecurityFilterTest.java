package org.jboss.errai.security.server;

import static org.jboss.errai.security.server.FormAuthenticationScheme.HOST_PAGE_INIT_PARAM;
import static org.jboss.errai.security.server.FormAuthenticationScheme.LOGIN_PAGE_INIT_PARAM;
import static org.mockito.Mockito.when;
import static org.picketlink.authentication.web.AuthenticationFilter.FORCE_REAUTHENTICATION_INIT_PARAM;

import java.util.Properties;

import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.security.server.mock.MockFilterConfig;
import org.jboss.errai.security.server.mock.MockHttpServletRequest;
import org.jboss.errai.security.server.mock.MockHttpSession;
import org.jboss.errai.security.server.mock.MockIdentity;
import org.jboss.errai.security.server.mock.MockServletContext;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.picketlink.Identity;
import org.picketlink.authentication.web.AuthenticationFilter;
import org.picketlink.authentication.web.HTTPAuthenticationScheme;
import org.picketlink.credential.DefaultLoginCredentials;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseSecurityFilterTest {

  /**
   * Configuration that can be passed to {@link UberFireSecurityFilter#init(javax.servlet.FilterConfig)}.
   */
  protected MockFilterConfig filterConfig;

  protected final String contextPath = "/test-context";

  /**
   * A mock HttpSession that mock requests can use. This value is returned as the session from the mock request.
   */
  protected MockHttpSession mockHttpSession;

  protected MockHttpServletRequest request;

  @Mock
  protected HttpServletResponse response;

  @Mock
  protected FilterChain filterChain;

  @Mock
  protected Instance<DefaultLoginCredentials> credentialsInstance;

  @Spy
  protected DefaultLoginCredentials credentials = new DefaultLoginCredentials();

  @Mock
  protected Instance<Identity> identityInstance;

  @Spy
  protected MockIdentity identity;

  @Mock(name = "applicationPreferredAuthSchemeInstance")
  protected Instance<HTTPAuthenticationScheme> preferredAuthFilterInstance;

  @Mock(name = "allAvailableAuthSchemesInstance")
  protected Instance<HTTPAuthenticationScheme> allAvailableAuthSchemesInstance;

  @InjectMocks
  protected AuthenticationFilter authFilter;

  @Mock
  protected AuthenticationService authService;
  
  @Mock
  protected Properties properties;

  @InjectMocks
  protected FormAuthenticationScheme formAuthenticationScheme;

  @Before
  public void setup() {
    filterConfig = new MockFilterConfig(new MockServletContext());
    filterConfig.setContextPath(contextPath);

    // useful minimum configuration. tests may overwrite these values before calling filter.init().
    filterConfig.initParams.put(HOST_PAGE_INIT_PARAM, "/dont/care/host");
    filterConfig.initParams.put(LOGIN_PAGE_INIT_PARAM, "/dont/care/login");
    filterConfig.initParams.put(FORCE_REAUTHENTICATION_INIT_PARAM, "true");

    mockHttpSession = new MockHttpSession();

    request = new MockHttpServletRequest(mockHttpSession, "POST", "/some/servlet/path", "/dont/care/login");

    identity.setCredentials(credentials);

    when(identityInstance.get()).thenReturn(identity);

    when(credentialsInstance.get()).thenReturn(credentials);

    when(preferredAuthFilterInstance.get()).thenReturn(formAuthenticationScheme);
  }

}
