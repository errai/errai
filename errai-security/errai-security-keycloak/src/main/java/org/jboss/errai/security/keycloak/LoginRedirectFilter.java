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

import java.io.IOException;

import javax.inject.Inject;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;

import org.keycloak.KeycloakSecurityContext;

/**
 * <p>
 * Used for forcing Keycloak logins in applications that wish to allow unauthenticated access to the
 * GWT host page. By adding this filter and a security constraint to a URL, a request to that URL
 * will redirect to a Keycloak login. After a successful login, this filter will redirect to a
 * path specified by the {@value #REDIRECT_PARAM_NAME} filter initParam.
 *
 * <p>
 * The {@link #REDIRECT_PARAM_NAME} is relative to the application context.
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
@WebFilter(filterName = "ErraiLoginRedirectFilter")
public class LoginRedirectFilter implements Filter {

  public static final String REDIRECT_PARAM_NAME = "redirectLocation";

  private String redirectLocation;

  @Inject
  private KeycloakAuthenticationService keycloakAuthService;

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    final String redirectParam = filterConfig.getInitParameter(REDIRECT_PARAM_NAME);
    redirectLocation = filterConfig.getServletContext().getContextPath();
    if (redirectParam != null) {
      redirectLocation += redirectParam;
    }
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response,
          final FilterChain chain) throws IOException, ServletException {
    final HttpServletResponse httpResponse = (HttpServletResponse) response;
    keycloakAuthService.setSecurityContext((KeycloakSecurityContext) request.getAttribute(KeycloakSecurityContext.class
            .getName()));
    httpResponse.sendRedirect(redirectLocation);
  }

  @Override
  public void destroy() {
  }
}
