/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
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

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueSession;
import org.mvel2.util.StringAppender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;

import static org.jboss.errai.bus.server.io.MessageFactory.createCommandMessage;

/**
 * The <tt>WeblogicAsyncServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Weblogic.
 */
public class WeblogicAsyncServlet extends AbstractErraiServlet {

  /**
   * Called by the server (via the <tt>service</tt> method) to allow a servlet to handle a GET request by supplying
   * a response
   *
   * @param httpServletRequest  - object that contains the request the client has made of the servlet
   * @param httpServletResponse - object that contains the response the servlet sends to the client
   * @throws IOException                    - if an input or output error is detected when the servlet handles the GET request
   * @throws javax.servlet.ServletException - if the request for the GET could not be handled
   */
  @Override
  protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
      throws ServletException, IOException {
    pollForMessages(sessionProvider.getSession(httpServletRequest.getSession(),
        httpServletRequest.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER)),
        httpServletRequest, httpServletResponse);
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

    final QueueSession session = sessionProvider.getSession(httpServletRequest.getSession(),
        httpServletRequest.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

    BufferedReader reader = httpServletRequest.getReader();
    StringAppender sb = new StringAppender(httpServletRequest.getContentLength());
    CharBuffer buffer = CharBuffer.allocate(10);

    int read;
    while ((read = reader.read(buffer)) > 0) {
      buffer.rewind();
      for (; read > 0; read--) {
        sb.append(buffer.get());
      }
      buffer.rewind();
    }

    Message m = createCommandMessage(session, sb.toString());
    if (m != null) {
      try {
        service.store(m);
      }
      catch (Exception e) {
        if (!e.getMessage().contains("expired")) {
          writeExceptionToOutputStream(httpServletResponse, e);
          return;
        }
      }
    }

    pollQueue(service.getBus().getQueue(session), httpServletRequest, httpServletResponse);
  }

  private void pollForMessages(QueueSession session, HttpServletRequest httpServletRequest,
                               HttpServletResponse httpServletResponse) throws IOException {
    try {
      final MessageQueue queue = service.getBus().getQueue(session);

      if (queue == null) {
        switch (getConnectionPhase(httpServletRequest)) {
          case CONNECTING:
          case DISCONNECTING:
            return;
        }
        sendDisconnectWithReason(httpServletResponse.getOutputStream(),
            "There is no queue associated with this session.");
      }

      pollQueue(queue, httpServletRequest, httpServletResponse);

    }
    catch (final Throwable t) {
      t.printStackTrace();

      httpServletResponse.setHeader("Cache-Control", "no-cache");
      httpServletResponse.setHeader("Pragma", "no-cache");
      httpServletResponse.setHeader("Expires", "-1");
      httpServletResponse.addHeader("Payload-Size", "1");
      httpServletResponse.setContentType("application/json");
      OutputStream stream = httpServletResponse.getOutputStream();

      stream.write('[');

      writeToOutputStream(stream, new MarshalledMessage() {
        public String getSubject() {
          return "ClientBusErrors";
        }

        public Object getMessage() {
          StringBuilder b = new StringBuilder("{ErrorMessage:\"").append(t.getMessage()).append("\",AdditionalDetails:\"");
          for (StackTraceElement e : t.getStackTrace()) {
            b.append(e.toString()).append("<br/>");
          }

          return b.append("\"}").toString();
        }
      });

      stream.write(']');
    }
  }

  private static void pollQueue(MessageQueue queue, HttpServletRequest httpServletRequest,
                                HttpServletResponse httpServletResponse) throws IOException {
    queue.heartBeat();

    httpServletResponse.setHeader("Cache-Control", "no-cache");
    httpServletResponse.setContentType("application/json");
    queue.poll(false, httpServletResponse.getOutputStream());
  }

  public static void main(String[] args) {
    System.out.println(String.valueOf(System.nanoTime()).length() + ":" + String.valueOf(System.currentTimeMillis()).length());
  }

}
