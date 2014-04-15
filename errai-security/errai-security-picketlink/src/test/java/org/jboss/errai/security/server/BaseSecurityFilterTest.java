package org.jboss.errai.security.server;

import static org.jboss.errai.security.server.FormAuthenticationScheme.*;
import static org.jboss.errai.security.server.PicketLinkAuthenticationFilter.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import javax.enterprise.inject.Instance;
import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.security.server.FormAuthenticationScheme;
import org.jboss.errai.security.server.mock.MockFilterConfig;
import org.jboss.errai.security.server.mock.MockHttpSession;
import org.jboss.errai.security.server.mock.MockIdentity;
import org.jboss.errai.security.server.mock.MockServletContext;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.runners.MockitoJUnitRunner;
import org.picketlink.Identity;
import org.picketlink.credential.DefaultLoginCredentials;

@RunWith(MockitoJUnitRunner.class)
public abstract class BaseSecurityFilterTest {

    /**
     * Configuration that can be passed to {@link UberFireSecurityFilter#init(javax.servlet.FilterConfig)}.
     */
    protected MockFilterConfig filterConfig;

    /**
     * A mock HttpSession that mock requests can use. This value is returned as the session from the mock request.
     */
    protected MockHttpSession mockHttpSession;

    @Mock
    protected HttpServletRequest request;

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

    @InjectMocks
    protected PicketLinkAuthenticationFilter uberFireFilter;

    @Before
    public void setup() {
        filterConfig = new MockFilterConfig( new MockServletContext() );

        // useful minimum configuration. tests may overwrite these values before calling filter.init().
        filterConfig.initParams.put( AUTH_TYPE_INIT_PARAM, FormAuthenticationScheme.class.getName() );
        filterConfig.initParams.put( HOST_PAGE_INIT_PARAM, "/dont/care" );
        filterConfig.initParams.put( FORCE_REAUTHENTICATION_INIT_PARAM, "true" );

        mockHttpSession = new MockHttpSession();

        when( request.getMethod() ).thenReturn( "POST" );
        when( request.getSession() ).thenReturn( mockHttpSession );
        when( request.getSession( anyBoolean() ) ).thenReturn( mockHttpSession );

        identity.setCredentials( credentials );

        when( identityInstance.get() ).thenReturn( identity );

        when( credentialsInstance.get() ).thenReturn( credentials );
    }

}
