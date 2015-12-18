/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
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

import static org.slf4j.LoggerFactory.getLogger;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;

import java.io.IOException;

/**
 * The default DefaultBlockingServlet which provides the HTTP-protocol gateway
 * between the server bus and the client buses.
 * <p/>
 * <h2>Configuration</h2>
 * <p/>
 * The DefaultBlockingServlet, as its name suggests, is normally configured as
 * an HTTP Servlet in the <code>web.xml</code> file:
 * <p/>
 * <pre>
 * {@code <servlet>}
 *   {@code <servlet-name>ErraiServlet</servlet-name>}
 *   {@code <servlet-class>org.jboss.errai.bus.server.servlet.DefaultBlockingServlet</servlet-class>}
 *   {@code <load-on-startup>1</load-on-startup>}
 * {@code </servlet>}
 *
 * {@code <servlet-mapping>}
 *   {@code <servlet-name>ErraiServlet</servlet-name>}
 *   {@code <url-pattern>*.erraiBus</url-pattern>}
 * {@code </servlet-mapping>}
 * </pre>
 * <p/>
 * Alternatively, the DefaultBlockingServlet can be deployed as a Servlet
 * Filter. This may be necessary in cases where an existing filter is configured
 * in the web application, and that filter interferes with the Errai Bus
 * requests. In this case, configuring DefaultBlockingServlet to handle
 * {@code *.erraiBus} requests ahead of other filters in web.xml will solve the
 * problem:
 * <p/>
 * <pre>
 * {@code <filter>}
 *   {@code <filter-name>ErraiServlet</filter-name>}
 *   {@code <filter-class>org.jboss.errai.bus.server.servlet.DefaultBlockingServlet</filter-class>}
 * {@code </filter>}
 *
 * {@code <filter-mapping>}
 *   {@code <filter-name>ErraiServlet</filter-name>}
 *   {@code <url-pattern>*.erraiBus</url-pattern>}
 * {@code </filter-mapping>}
 *
 * {@code <!-- other filter-mapping and servlet-mapping elements go here -->}
 * </pre>
 */

public class ChaosMonkeyServlet extends DefaultBlockingServlet {
  
  private static final Logger log = getLogger(ChaosMonkeyServlet.class);

  @Override
  public void init(ServletConfig config) throws ServletException {
    super.init(config);
  }

  @Override
  public void initAsFilter(FilterConfig config) throws ServletException {
    super.initAsFilter(config);
  }

  @Override
  public void init(final FilterConfig filterConfig) throws ServletException {
    super.initAsFilter(filterConfig);
  }


  /**
   * Called by the server (via the <tt>service</tt> method) to allow a servlet to handle a GET request by supplying
   * a response
   *
   * @param httpServletRequest
   *     - object that contains the request the client has made of the servlet
   * @param httpServletResponse
   *     - object that contains the response the servlet sends to the client
   *
   * @throws java.io.IOException
   *     - if an input or output error is detected when the servlet handles the GET request
   * @throws javax.servlet.ServletException
   *     - if the request for the GET could not be handled
   */
  @Override
  protected void doGet(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
      throws ServletException {

    try {
      if (Math.random() > 0.7d) {
        httpServletResponse.setStatus(404);
        httpServletResponse.getWriter().println("The chaos monkey strikes again!");
        httpServletResponse.flushBuffer();
      }
      else if (Math.random() < 0.3d) {
        httpServletResponse.setStatus(401);
        httpServletResponse.getWriter().println("The chaos monkey strikes again!");
        httpServletResponse.flushBuffer();
      }
      else {
        super.doGet(httpServletRequest, httpServletResponse);
      }
    }
    catch (IOException ioe) {
      log.debug("Chaos Monkey ran into problem", ioe);
    }
  }

  /**
   * Called by the server (via the <code>service</code> method) to allow a servlet to handle a POST request, by
   * sending the request
   *
   * @param httpServletRequest
   *     - object that contains the request the client has made of the servlet
   * @param httpServletResponse
   *     - object that contains the response the servlet sends to the client
   *
   * @throws java.io.IOException
   *     - if an input or output error is detected when the servlet handles the request
   * @throws javax.servlet.ServletException
   *     - if the request for the POST could not be handled
   */
  @Override
  protected void doPost(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse) throws ServletException {
    
    try {
      if (Math.random() > 0.7d) {
        httpServletResponse.setStatus(404);
        httpServletResponse.getWriter().println("The chaos monkey strikes again!");
        httpServletResponse.flushBuffer();
      }
      else if (Math.random() < 0.3d) {
        httpServletResponse.setStatus(401);
        httpServletResponse.getWriter().println("The chaos monkey strikes again!");
        httpServletResponse.flushBuffer();
      }
      else {
        super.doPost(httpServletRequest, httpServletResponse);
      }
    } 
    catch (IOException ioe) {
      log.debug("Chaos Monkey ran into problem", ioe);
    }
  }


  /**
   * Services this request in the same way as it would be serviced if configured
   * as a Servlet. Does not invoke any filters further down the chain. See the
   * class-level comment for the reason why this servlet might be configured as a
   * filter.
   */
  @Override
  public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain)
      throws IOException, ServletException {
    if (Math.random() > 0.7d) {
     // response.setStatus(404);
      response.getWriter().println("The chaos monkey strikes again!");
      response.flushBuffer();
    }
    else {
      super.doFilter(request, response, chain);
    }
  }
}
