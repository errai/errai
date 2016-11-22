/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.security.server.servlet;

import static org.jboss.errai.common.server.FilterCacheUtil.getCharResponseWrapper;
import static org.jboss.errai.common.server.FilterCacheUtil.noCache;
import static org.jboss.errai.security.Properties.USER_ON_HOSTPAGE_ENABLED;

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
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.common.server.FilterCacheUtil.CharResponseWrapper;
import org.jboss.errai.marshalling.server.MappingContextSingleton;
import org.jboss.errai.marshalling.server.ServerMarshalling;
import org.jboss.errai.security.server.properties.ErraiAppProperties;
import org.jboss.errai.security.shared.api.SecurityConstants;
import org.jboss.errai.security.shared.api.identity.User;
import org.jboss.errai.security.shared.service.AuthenticationService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Serializes and adds the authenticated user to a JavaScript variable in the
 * application's host page. This is useful in case the login page lives outside
 * the GWT application as it makes instances of {@link User} immediately
 * injectable (without requiring a server round-trip.)
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@WebFilter(filterName = "ErraiUserHostPageFilter", urlPatterns = { "/index.jsp", "/index.html" })
public class UserHostPageFilter implements Filter {

  @Inject
  private AuthenticationService authenticationService;

  @Inject
  @ErraiAppProperties
  private Properties properties;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // Initialize server side marshaller
    MappingContextSingleton.get();
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
          ServletException {

    if (!isUserOnHostPageEnabled()) {
      chain.doFilter(request, noCache((HttpServletResponse) response));
    }
    else {
      final CharResponseWrapper wrappedResponse = getCharResponseWrapper((HttpServletResponse) response);
      chain.doFilter(request, noCache(wrappedResponse));

      final User user = authenticationService.getUser();
      final String output;

      if (user != null) {
        final String injectedScript = "<script>var " +
                SecurityConstants.ERRAI_SECURITY_CONTEXT_DICTIONARY + "  = " +
                securityContextJson(user) + "; </script>";

        final Document document = Jsoup.parse(wrappedResponse.toString());
        document.head().append(injectedScript);
        output = document.html();
      }
      else {
        output = wrappedResponse.toString();
      }

      final byte[] outputBytes = output.getBytes("UTF-8");
      response.setContentLength(outputBytes.length);
      response.getOutputStream().write(outputBytes);
    }
  }

  String securityContextJson(final User user) {
    final String userJson = ServerMarshalling.toJSON(user);

    return "{\"" + SecurityConstants.DICTIONARY_USER + "\": " + userJson + "}";
  }

  private boolean isUserOnHostPageEnabled() {
    if (properties.containsKey(USER_ON_HOSTPAGE_ENABLED)) {
      return Boolean.parseBoolean(properties.getProperty(USER_ON_HOSTPAGE_ENABLED));
    }
    return false;
  }

}
