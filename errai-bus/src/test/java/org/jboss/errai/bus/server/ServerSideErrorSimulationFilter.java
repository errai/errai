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

package org.jboss.errai.bus.server;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A filter that can be configured to respond to all requests with a given error
 * code. This can be used to simulates a variety of server-side conditions that
 * will lead the client-side bus to try to reconnect.
 * <p>
 * There are two ways to change the filter's behaviour:
 * <ol>
 *  <li>On the server, just set the static {@link #errorCode} field to a
 * non-zero value (for example, try 401, 404, or 500).
 *  <li>From the client, send a GET request that this filter will intercept with a parameter called
 *  "errorCode". For example, this filter is normally mapped to {@code *.erraiBus} so
 *  {@code GET /myapp/errorSimulator.erraiBus?errorCode=401} should work.
 * </ol>
 * 
 * @author jfuerth
 */
public class ServerSideErrorSimulationFilter implements Filter {

  /**
   * If this field is set to a non-zero value, this filter will respond to all
   * requests with that HTTP status. When this field has a 0 value, the filter
   * has no effect.
   */
  public static volatile int errorCode = 0;
  
  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    // no op
  }

  @Override
  public void destroy() {
    // no op
  }

  @Override
  public void doFilter(ServletRequest req, ServletResponse resp,
          FilterChain chain) throws IOException, ServletException {
    
    HttpServletRequest request = (HttpServletRequest) req;
    HttpServletResponse response = (HttpServletResponse) resp;
    
    final int errorCode = ServerSideErrorSimulationFilter.errorCode;
    if (request.getParameter("errorCode") != null) {
      ServerSideErrorSimulationFilter.errorCode = Integer.parseInt(request.getParameter("errorCode"));
    }
    else if (errorCode != 0) {
      ((HttpServletResponse) response).sendError(errorCode);
    }
    else {
      chain.doFilter(request, response);
    }
  }

}
