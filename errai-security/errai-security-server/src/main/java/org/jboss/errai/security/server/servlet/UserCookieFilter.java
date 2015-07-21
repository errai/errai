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
import org.jboss.errai.security.shared.service.AuthenticationService;

/**
 * Sets the Errai user cookie if the {@link org.jboss.errai.security.Properties#USER_COOKIE_ENABLED}
 * property is set to true. This filter should be used on the host page of Errai an application.
 *
 * @deprecated Use {@link UserHostPageFilter} for form based login.
 * @author Max Barkley <mbarkley@redhat.com>
 */
@WebFilter(filterName = "ErraiUserCookieFilter", urlPatterns = { "/index.jsp", "/index.html" })
@Deprecated
public class UserCookieFilter implements Filter {

  static {
    MappingContextSingleton.get();
  }

  @Inject
  private AuthenticationService authService;

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

    maybeSetUserCookie(httpResponse);

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
  private boolean maybeSetUserCookie(final HttpServletResponse response) {
    if (properties.containsKey(USER_COOKIE_ENABLED)) {
      final Boolean userCookieEnabled = Boolean.parseBoolean(properties.getProperty(USER_COOKIE_ENABLED));
      if (userCookieEnabled) {
        final Cookie userCookie = new Cookie(UserCookieEncoder.USER_COOKIE_NAME,
                UserCookieEncoder.toCookieValue(authService.getUser()));
        response.addCookie(userCookie);
        return true;
      }
    }

    return false;
  }
}
