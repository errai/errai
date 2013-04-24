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

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.server.QueueUnavailableException;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueActivationCallback;
import org.jboss.errai.bus.server.io.OutputStreamWriteAdapter;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * An implementation of {@link AbstractErraiServlet} leveraging asynchronous support of Servlet 3.0.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 * @author Mike Brock
 */
public class StandardAsyncServlet extends AbstractErraiServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(final HttpServletRequest request, final HttpServletResponse response) throws ServletException,
      IOException {

    final QueueSession session = sessionProvider.createOrGetSession(request.getSession(), getClientId(request));
    final MessageQueue queue = service.getBus().getQueue(session);

    if (queue == null) {
      switch (getConnectionPhase(request)) {
        case CONNECTING:
        case DISCONNECTING:
          return;
      }
      sendDisconnectDueToSessionExpiry(response);
      return;
    }

    queue.heartBeat();

    final OutputStreamWriteAdapter writer = new OutputStreamWriteAdapter(response.getOutputStream());

    if (isSSERequest(request)) {
      prepareSSE(response);
      prepareSSEContinue(response);

      response.getOutputStream().flush();

      final AsyncContext asyncContext = request.startAsync();
      asyncContext.setTimeout(getSSETimeout());
      queue.setTimeout(getSSETimeout() + 5000);

      asyncContext.addListener(new AsyncListener() {
        @Override
        public void onComplete(final AsyncEvent event) throws IOException {
          synchronized (queue.getActivationLock()) {
            queue.setActivationCallback(null);
            asyncContext.complete();
          }
        }

        @Override
        public void onTimeout(final AsyncEvent event) throws IOException {
          onComplete(event);
        }

        @Override
        public void onError(final AsyncEvent event) throws IOException {
          queue.setActivationCallback(null);
        }

        @Override
        public void onStartAsync(final AsyncEvent event) throws IOException {
        }
      });

      synchronized (queue.getActivationLock()) {
        if (queue.messagesWaiting()) {
          queue.poll(writer);
          writer.write(SSE_TERMINATION_BYTES);
        }

        queue.setActivationCallback(new QueueActivationCallback() {
          @Override
          public void activate(final MessageQueue queue) {
            try {
              queue.setActivationCallback(null);
              queue.poll(writer);

              writer.write(SSE_TERMINATION_BYTES);

              queue.heartBeat();
              writer.flush();

              prepareSSEContinue(response);
            }
            catch (final Throwable t) {
              try {
                writeExceptionToOutputStream((HttpServletResponse) asyncContext.getResponse(), t);
              }
              catch (IOException e) {
                throw new RuntimeException("Failed to write exception to output stream", e);
              }
            }
          }
        });

        writer.flush();
      }
    }

    else {
      final AsyncContext asyncContext = request.startAsync();
      asyncContext.setTimeout(60000);
      queue.setTimeout(65000);

      asyncContext.addListener(new AsyncListener() {
          @Override
          public void onComplete(final AsyncEvent event) throws IOException {
            synchronized (queue.getActivationLock()) {
              queue.setActivationCallback(null);
              asyncContext.complete();
            }
          }

          @Override
          public void onTimeout(final AsyncEvent event) throws IOException {
            onComplete(event);
          }

          @Override
          public void onError(final AsyncEvent event) throws IOException {
            queue.setActivationCallback(null);
          }

          @Override
          public void onStartAsync(final AsyncEvent event) throws IOException {
          }
        });

      synchronized (queue.getActivationLock()) {
        if (queue.messagesWaiting()) {
          queue.poll(writer);
          asyncContext.complete();
          return;
        }

        queue.setActivationCallback(new QueueActivationCallback() {
          @Override
          public void activate(final MessageQueue queue) {
            try {
              queue.poll(writer);
              queue.setActivationCallback(null);

              queue.heartBeat();
              writer.flush();
            }
            catch (final Throwable t) {
              try {
                writeExceptionToOutputStream((HttpServletResponse) asyncContext.getResponse(), t);
              }
              catch (IOException e) {
                throw new RuntimeException("Failed to write exception to output stream", e);
              }
            }
            finally {
              asyncContext.complete();
            }
          }
        });
        writer.flush();
      }
    }
  }

  @Override
  protected void doPost(final HttpServletRequest request, final HttpServletResponse response) throws ServletException, IOException {
    final QueueSession session = sessionProvider.createOrGetSession(request.getSession(), getClientId(request));
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
    catch (Exception e) {
      final String message = e.getMessage();
      if (message == null) {
        e.printStackTrace();
      }
      else if (!message.contains("expired")) {
        writeExceptionToOutputStream(response, e);
      }
    }
  }
}