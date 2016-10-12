/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates.
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
package org.jboss.errai.cdi.server.scripts;

import static org.jboss.errai.common.server.FilterCacheUtil.getCharResponseWrapper;
import static org.jboss.errai.common.server.FilterCacheUtil.noCache;

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

import org.jboss.errai.common.server.FilterCacheUtil.CharResponseWrapper;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Adds all registered scripts from {@link ScriptRegistry} to the &lt;head&gt;
 * of the host page.
 * 
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@WebFilter(filterName = "ErraiHostPageScriptInjectorFilter", urlPatterns = { "/index.jsp", "/index.html" })
public class HostPageScriptInjectorFilter implements Filter {

  @Inject
  private ScriptRegistry registry;

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
  }

  @Override
  public void destroy() {
  }

  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {

    final HttpServletResponse httpServletResponse = (HttpServletResponse) response;

    /*
     * ===== DANGER WILL ROBISON =====
     * 
     * When Errai is not being executed from a Java EE server (or a Servlet container
     * with CDI glued on), the ScriptRegistry will not be injected.  While the null
     * check below is unneeded in many cases, if you're running super-dev-mode
     * with the default Jetty implementation this null check is required.
     */
    if (registry !=null && !registry.isEmpty()) {
      final CharResponseWrapper wrappedResponse = getCharResponseWrapper((HttpServletResponse) response);
      chain.doFilter(request, noCache(wrappedResponse));

      final Document document = Jsoup.parse(wrappedResponse.toString());
      registry.getAllScripts().forEach(s -> document.head().append("<script src=\"" + s + "\"/>"));
      final byte[] outputBytes = document.html().getBytes("UTF-8");
      response.setContentLength(outputBytes.length);
      response.getOutputStream().write(outputBytes);
    }
    else {
      chain.doFilter(request, noCache(httpServletResponse));
    }
  }

}
