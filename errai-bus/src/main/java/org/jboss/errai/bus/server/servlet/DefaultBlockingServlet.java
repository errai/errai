/*
 * Copyright 2012 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server.servlet;

import static org.jboss.errai.bus.server.io.MessageFactory.createCommandMessage;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.io.OutputStreamWriteAdapter;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;

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

public class DefaultBlockingServlet extends AbstractErraiServlet implements Filter {


  /**
   * Called by the server (via the <tt>service</tt> method) to allow a servlet to handle a GET request by supplying
   * a response
   *
   * @param httpServletRequest  - object that contains the request the client has made of the servlet
   * @param httpServletResponse - object that contains the response the servlet sends to the client
   * @throws IOException      - if an input or output error is detected when the servlet handles the GET request
   * @throws ServletException - if the request for the GET could not be handled
   */
  @Override
  protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
          throws ServletException, IOException {
    pollForMessages(sessionProvider.createOrGetSession(httpServletRequest.getSession(true),
            httpServletRequest.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER)),
            httpServletRequest, httpServletResponse, ErraiServiceConfigurator.LONG_POLLING);
  }

  /**
   * Called by the server (via the <code>service</code> method) to allow a servlet to handle a POST request, by
   * sending the request
   *
   * @param httpServletRequest  - object that contains the request the client has made of the servlet
   * @param httpServletResponse - object that contains the response the servlet sends to the client
   * @throws IOException      - if an input or output error is detected when the servlet handles the request
   * @throws ServletException - if the request for the POST could not be handled
   */
  @Override
  protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
          throws ServletException, IOException {
    final QueueSession session = sessionProvider.createOrGetSession(httpServletRequest.getSession(true),
            httpServletRequest.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

    service.store(createCommandMessage(session, httpServletRequest));

    pollForMessages(session, httpServletRequest, httpServletResponse, false);
  }

  private void pollForMessages(QueueSession session, HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse, boolean wait) throws IOException {
    try {
      // note about caching: clients now include a uniquifier in a request parameter called "z"
      // so no-cache headers are now unnecessary.
      httpServletResponse.setContentType("application/json");

      final MessageQueue queue = service.getBus().getQueue(session);

      ServletOutputStream outputStream = httpServletResponse.getOutputStream();
      if (queue == null) {
        switch (getConnectionPhase(httpServletRequest)) {
          case CONNECTING:
          case DISCONNECTING:
            return;
        }

        sendDisconnectDueToSessionExpiry(outputStream);
        return;
      }

      queue.heartBeat();

      queue.poll(wait, new OutputStreamWriteAdapter(outputStream));

      outputStream.close();
    }
    catch (final Throwable t) {
      t.printStackTrace();
      writeExceptionToOutputStream(httpServletResponse, t);
    }
  }

  @Override
  public void init(FilterConfig filterConfig) throws ServletException {
    super.initAsFilter(filterConfig);
  }

  /**
   * Services this request in the same way as it would be serviced if configured
   * as a Servlet. Does not invoke any filters further down the chain. See the
   * class-level comment for the reason why this servlet might be configured as a
   * filter.
   */
  @Override
  public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
          throws IOException, ServletException {
    service(request, response);
  }
}