/*
 * Copyright (C) 2016 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.bus.server.servlet;

import static org.jboss.errai.common.client.framework.Constants.ERRAI_CSRF_TOKEN_VAR;
import static org.jboss.errai.common.server.FilterCacheUtil.*;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class CSRFTokenFilter implements Filter {

  private static Logger log = LoggerFactory.getLogger(CSRFTokenFilter.class);

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
          throws IOException, ServletException {
    final HttpServletRequest httpRequest = (HttpServletRequest) request;
    ensureSessionHasToken(httpRequest.getSession(false));

    switch (httpRequest.getMethod().toUpperCase()) {
    case "POST":
    case "PUT":
    case "DELETE": {
      if (CSRFTokenCheck.INSTANCE.isInsecure(httpRequest, log)) {
        CSRFTokenCheck.INSTANCE.prepareResponse(httpRequest, (HttpServletResponse) response, log);
        return;
      }
    }
    case "GET": {
      final HttpServletResponse responseWrapper = noCache(getCharResponseWrapper((HttpServletResponse) response));
      chain.doFilter(httpRequest, responseWrapper);
      final HttpSession session = httpRequest.getSession(false);

      final byte[] bytes;
      final String output;
      final String responseContentType = responseWrapper.getContentType();
      if (session != null && responseContentType != null && responseContentType.toLowerCase().startsWith("text/html")) {

        CSRFTokenCheck.INSTANCE.prepareSession(session, log);
        final Document document = Jsoup.parse(responseWrapper.toString());
        String injectedScript = "<script>var " + ERRAI_CSRF_TOKEN_VAR + " = '" + CSRFTokenCheck.getToken(session) + "';</script>";
        document.head().prepend(injectedScript);
        output = document.html();
      }
      else {
        output = responseWrapper.toString();
      }

      bytes = output.getBytes("UTF-8");
      response.setContentLength(bytes.length);
      response.getWriter().print(output);

      return;
    }
    }

    chain.doFilter(request, response);
  }

  private void ensureSessionHasToken(final HttpSession session) {
    if (session != null) {
      CSRFTokenCheck.INSTANCE.prepareSession(session, log);
    }
  }
}
