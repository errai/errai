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

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.server.QueueUnavailableException;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueActivationCallback;
import org.jboss.errai.bus.server.io.OutputStreamWriteAdapter;
import org.slf4j.Logger;

/**
 * An implementation of {@link AbstractErraiServlet} leveraging asynchronous support of Servlet 3.0.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class StandardAsyncServlet extends AbstractErraiServlet {
  private static final Logger log = getLogger(StandardAsyncServlet.class);
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
    final QueueSession session = sessionProvider.createOrGetSession(request.getSession(), getClientId(request));
    session.setAttribute("NoSSE", Boolean.TRUE);
    
    final MessageQueue queue = service.getBus().getQueue(session);
    if (queue == null) {
      switch (getConnectionPhase(request)) {
        case CONNECTING:
        case DISCONNECTING:
          return;
      }
      try {
        sendDisconnectDueToSessionExpiry(response);
      } 
      catch (IOException ioe) {
        log.debug("Failed to inform client that session expired", ioe);
      }
      return;
    }
    queue.heartBeat();

    
    final AsyncContext asyncContext = request.startAsync();
    asyncContext.setTimeout(60000);
    queue.setTimeout(65000);
    
    asyncContext.addListener(new AsyncListener() {
        @Override
        public void onComplete(final AsyncEvent event) throws IOException {
          clearActivationCallback(queue);
        }

        @Override
        public void onTimeout(final AsyncEvent event) throws IOException {
          clearActivationCallback(queue);
          asyncContext.complete();
        }

        @Override
        public void onError(final AsyncEvent event) throws IOException {
          clearActivationCallback(queue);
        }

        @Override
        public void onStartAsync(final AsyncEvent event) throws IOException {
        }
      });

    synchronized (queue.getActivationLock()) {
      if (queue.messagesWaiting()) {
        try {
          queue.poll(new OutputStreamWriteAdapter(asyncContext.getResponse().getOutputStream()));
          asyncContext.complete();
        } 
        catch(IOException ioe) {
          log.debug("Problem when polling for new messages", ioe);       
        }
        return;
      }

      queue.setActivationCallback(new QueueActivationCallback() {
        @Override
        public void activate(final MessageQueue queue) {
          try {
            queue.poll(new OutputStreamWriteAdapter(asyncContext.getResponse().getOutputStream()));
            queue.setActivationCallback(null);
            queue.heartBeat();
          }
          catch (IOException e) {
            log.debug("Closing queue with id: " + queue.getSession().getSessionId() + " due to IOException", e);
          }
          catch (final Throwable t) {
            try {
              writeExceptionToOutputStream((HttpServletResponse) asyncContext.getResponse(), t);
            }
            catch (Throwable t2) {
              log.debug("Failed to write exception to dead client", t2);
            }
          }
          finally {
            asyncContext.complete();
          }
        }
      });
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException {
    final QueueSession session = sessionProvider.createOrGetSession(request.getSession(), getClientId(request));
    session.setAttribute("NoSSE", Boolean.TRUE);
    try {
      try {
        service.store(createCommandMessage(session, request));
      }
      catch (QueueUnavailableException e) {
        sendDisconnectDueToSessionExpiry(response);
        return;
      }

      final MessageQueue queue = service.getBus().getQueue(session);
      if (queue != null) {
        if (shouldWait(request)) {
          doGet(request, response);
        }
        else {
          queue.poll(new OutputStreamWriteAdapter(response.getOutputStream()));
        }
      }
    }
    catch (final IOException ioe) {
      log.debug("Problem when storing message", ioe);
    }
    catch (Exception e) {
      final String message = e.getMessage();
      if (message == null) {
        log.debug("Problem when storing message", e);
      }
      else if (!message.contains("expired")) {
        try {
          writeExceptionToOutputStream(response, e);
        } 
        catch (IOException ioe) {
          log.debug("Couldn't write exception to output stream", ioe);
        }
      }
    }
  }
  
  private void clearActivationCallback(final MessageQueue queue) {
    synchronized (queue.getActivationLock()) {
      queue.setActivationCallback(null);
    }
  }
}
