/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueActivationCallback;

/**
 * An implementation of {@link AbstractErraiServlet} leveraging asynchronous support of Servlet 3.0.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class StandardAsyncServlet extends AbstractErraiServlet {
  private static final long serialVersionUID = 1L;

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException,
      IOException {

    final QueueSession session = sessionProvider.getSession(request.getSession(),
        request.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

    final MessageQueue queue = service.getBus().getQueue(session);
    if (queue == null) {
      switch (getConnectionPhase(request)) {
      case CONNECTING:
      case DISCONNECTING:
        return;
      }
      sendDisconnectDueToSessionExpiry(response.getOutputStream());
      return;
    }

    response.setContentType("application/json");
    if (queue.messagesWaiting()) {
      queue.poll(false, response.getOutputStream());
      return;
    }

    final AsyncContext asyncContext = request.startAsync();
    asyncContext.addListener(new AsyncListener() {
      @Override
      public void onTimeout(AsyncEvent event) throws IOException {
        poll(queue, asyncContext);
        asyncContext.complete();
      }

      @Override
      public void onComplete(AsyncEvent event) throws IOException {}

      @Override
      public void onError(AsyncEvent event) throws IOException {}

      @Override
      public void onStartAsync(AsyncEvent event) throws IOException {}
    });

    queue.setActivationCallback(new QueueActivationCallback() {
      @Override
      public void activate(MessageQueue queue) {
        try {
          poll(queue, asyncContext);
        }
        catch (final Throwable t) {
          try {
            writeExceptionToOutputStream((HttpServletResponse)asyncContext.getResponse(), t);
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
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
    final QueueSession session = sessionProvider.getSession(request.getSession(),
            request.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));
    try {
      service.store(createCommandMessage(session, request));
    }
    catch (Exception e) {
      if (!e.getMessage().contains("expired")) {
        writeExceptionToOutputStream(response, e);
      }
    }
  }

  private void poll(MessageQueue queue, AsyncContext asyncContext) throws IOException {
    if (queue == null) return;
    queue.setActivationCallback(null);
    queue.heartBeat();
    queue.poll(false, asyncContext.getResponse().getOutputStream());
  }
}