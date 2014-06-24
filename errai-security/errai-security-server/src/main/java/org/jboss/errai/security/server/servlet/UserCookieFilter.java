package org.jboss.errai.security.server.servlet;

import static org.jboss.errai.security.Properties.USER_COOKIE_ENABLED;

import java.io.IOException;
import java.util.Properties;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.security.server.properties.ErraiAppProperties;
import org.jboss.errai.security.shared.api.UserCookieEncoder;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * Sets the Errai user cookie if the {@link org.jboss.errai.security.Properties#USER_COOKIE_ENABLED}
 * property is set to true. This filter should be used on the host page of Errai an application.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@WebFilter(filterName="ErraiUserCookieFilter")
public class UserCookieFilter implements Filter {

  static {
    MappingContextSingleton.get();
  }

  @Inject
  private AuthenticationService keycloakAuthService;

  @Inject
  @ErraiAppProperties
  private Properties properties;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
          throws IOException, ServletException {
    final HttpServletResponse httpResponse = (HttpServletResponse) response;

    final User keycloakUser = keycloakAuthService.getUser();
    maybeSetUserCookie(keycloakUser, httpResponse);

    chain.doFilter(request, response);
  }

  @Override
  public void destroy() {
  }

  /**
   * Add an Errai User cookie to the response if the property has been enabled.
   *
   * @param user
   *          The user to encode.
   * @param response
   *          The response to add a cookie to.
   * @return True iff the cookie was added.
   */
  private boolean maybeSetUserCookie(final User user, final HttpServletResponse response) {
    if (properties.containsKey(USER_COOKIE_ENABLED)) {
      final Boolean userCookieEnabled = (Boolean) properties.get(USER_COOKIE_ENABLED);
      if (userCookieEnabled) {
        final Cookie userCookie = new Cookie(UserCookieEncoder.USER_COOKIE_NAME,
                UserCookieEncoder.toCookieValue(user));
        response.addCookie(userCookie);
        return true;
      }
    }

    return false;
  }
}
