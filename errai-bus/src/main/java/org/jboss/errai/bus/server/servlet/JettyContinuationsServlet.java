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

import static org.jboss.errai.bus.server.io.MessageFactory.createCommandMessage;
import static org.slf4j.LoggerFactory.getLogger;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.server.QueueUnavailableException;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueActivationCallback;
import org.jboss.errai.bus.server.io.OutputStreamWriteAdapter;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;
import org.slf4j.Logger;

/**
 * The <tt>JettyContinuationsServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Jetty Continuations.
 */
public class JettyContinuationsServlet extends AbstractErraiServlet {
  private static final Logger log = getLogger(JettyContinuationsServlet.class);
  /**
   * Called by the server (via the <tt>service</tt> method) to allow a servlet to handle a GET request by supplying
   * a response
   *
   * @param httpServletRequest
   *     - object that contains the request the client has made of the servlet
   * @param httpServletResponse
   *     - object that contains the response the servlet sends to the client
   *
   * @throws IOException
   *     - if an input or output error is detected when the servlet handles the GET request
   * @throws ServletException
   *     - if the request for the GET could not be handled
   */
  @Override
  protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
      throws ServletException, IOException {
    pollForMessages(sessionProvider.createOrGetSession(httpServletRequest.getSession(true),
        getClientId(httpServletRequest)),
        httpServletRequest, httpServletResponse, true);
  }

  /**
   * Called by the server (via the <code>service</code> method) to allow a servlet to handle a POST request, by
   * sending the request.
   *
   * @param httpServletRequest
   *     - object that contains the request the client has made of the servlet
   * @param httpServletResponse
   *     - object that contains the response the servlet sends to the client
   *
   * @throws IOException
   *     - if an input or output error is detected when the servlet handles the request
   * @throws ServletException
   *     - if the request for the POST could not be handled
   */
  @Override
  protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException {

    final QueueSession session = sessionProvider.createOrGetSession(httpServletRequest.getSession(true),
        getClientId(httpServletRequest));

    session.setAttribute("NoSSE", Boolean.TRUE);

    try {
      service.store(createCommandMessage(session, httpServletRequest));
    }
    catch (IOException ioe) {
      log.debug("Problem when storing message", ioe);
    }
    catch (QueueUnavailableException e) {
      try {
        sendDisconnectDueToSessionExpiry(httpServletResponse);
      } catch (IOException ioe) {
        log.debug("Failed to inform client that session expired", ioe);
      }
      return;
    }

    pollForMessages(session, httpServletRequest, httpServletResponse, shouldWait(httpServletRequest));
  }

  private void pollForMessages(QueueSession session, HttpServletRequest httpServletRequest, 
          HttpServletResponse httpServletResponse, boolean wait) {
    
    try {
      final MessageQueue queue = service.getBus().getQueue(session);
      if (queue == null) {
        switch (getConnectionPhase(httpServletRequest)) {
          case CONNECTING:
          case DISCONNECTING:
            return;
        }

        sendDisconnectDueToSessionExpiry(httpServletResponse);

        return;
      }
      queue.heartBeat();

      if (wait) {
        synchronized (queue.getActivationLock()) {
          final Continuation cont = ContinuationSupport.getContinuation(httpServletRequest, queue);

          if (!cont.isResumed() && !queue.messagesWaiting()) {
            queue.setActivationCallback(new JettyQueueActivationCallback(cont));
            if (cont.suspend(30 * 1000)) {
              return;
            }
          }
        }
      }

      pollQueue(queue, httpServletResponse.getOutputStream(), httpServletResponse);
    }
    catch (RetryRequest r) {
      /**
       * This *must* be caught and re-thrown to work property with Jetty.
       */

      throw r;
    }
    catch (final IOException io) {
      log.debug("Problem when polling for new messages", io);
    }
    catch (final Throwable t) {
      t.printStackTrace();

      httpServletResponse.setHeader("Cache-Control", "no-cache");
      httpServletResponse.setHeader("Pragma", "no-cache");
      httpServletResponse.setHeader("Expires", "-1");
      httpServletResponse.addHeader("Payload-Size", "1");
      httpServletResponse.setContentType("application/json");


      StringBuilder b = new StringBuilder("{Error" +
                   "Message:\"").append(t.getMessage()).append("\",AdditionalDetails:\"");
               for (StackTraceElement e : t.getStackTrace()) {
                 b.append(e.toString()).append("<br/>");
               }
       b.append("\"}").toString();

       try {
         writeToOutputStream(httpServletResponse.getOutputStream(), b.toString());
       }
       catch (final IOException io) {
         log.debug("Failed to write error to output stream", io);
       }
    }
  }

  private static boolean pollQueue(MessageQueue queue, OutputStream stream,
                                   HttpServletResponse httpServletResponse) throws IOException {
    if (queue == null) return false;
    queue.heartBeat();

    httpServletResponse.setHeader("Cache-Control", "no-cache");
    httpServletResponse.setHeader("Pragma", "no-cache");
    httpServletResponse.setHeader("Expires", "-1");
    httpServletResponse.setContentType("application/json");
    return queue.poll(new OutputStreamWriteAdapter(stream));
  }

  private static class JettyQueueActivationCallback implements QueueActivationCallback {
    private final Continuation cont;

    private JettyQueueActivationCallback(Continuation cont) {
      this.cont = cont;
    }

    @Override
    public void activate(MessageQueue queue) {
      synchronized (queue.getActivationLock()) {
        queue.setActivationCallback(null);
        cont.resume();
      }
    }
  }
}
