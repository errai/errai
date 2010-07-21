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

import com.google.inject.Singleton;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueSession;
import org.mvel2.util.StringAppender;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.List;

import static org.jboss.errai.bus.server.io.MessageFactory.createCommandMessage;

/**
 * The default DefaultBlockingServlet which provides the HTTP-protocol gateway between the server bus and the client buses.
 */
@Singleton
public class DefaultBlockingServlet extends AbstractErraiServlet {
    /**
     * Creates an instance of the <tt>DefaultBlockingServlet</tt>. Does nothing else
     */
    public DefaultBlockingServlet() {
    }

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
        pollForMessages(sessionProvider.getSession(httpServletRequest.getSession(),
                httpServletRequest.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER)),
                httpServletRequest, httpServletResponse, true);
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
        if (contextClassLoader == null) contextClassLoader = Thread.currentThread().getContextClassLoader();


        final QueueSession session = sessionProvider.getSession(httpServletRequest.getSession(),
                httpServletRequest.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

        try {
//            ServletInputStream inputStream = httpServletRequest.getInputStream();
//            BufferedReader reader = new BufferedReader(
//                    new InputStreamReader(inputStream, "UTF-8")
//            );
//            StringAppender sb = new StringAppender(httpServletRequest.getContentLength());
//            CharBuffer buffer = CharBuffer.allocate(10);
//
//            int read;
//            while ((read = reader.read(buffer)) > 0) {
//                buffer.rewind();
//                for (; read > 0; read--) {
//                    sb.append(buffer.get());
//                }
//                buffer.rewind();
//            }

            Message m = createCommandMessage(session, httpServletRequest.getInputStream(), contextClassLoader);
            if (m != null) service.store(m);

        }
        catch (Throwable e) {
            // handle gracefully
            System.out.println("Error: https://jira.jboss.org/jira/browse/ERRAI-37");
            e.printStackTrace();
            httpServletResponse.setStatus(503); // Service Unavailable
        }

        pollForMessages(session, httpServletRequest, httpServletResponse, false);
    }

    private void pollForMessages(QueueSession session, HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse, boolean wait) throws IOException {
        try {
            final MessageQueue queue = service.getBus().getQueue(session);

            if (queue == null) {
                sendDisconnectWithReason(httpServletResponse.getOutputStream(),
                        "There is no queue associated with this session.");
                return;
            }

            queue.heartBeat();

            List<MarshalledMessage> messages = queue.poll(wait).getMessages();

            httpServletResponse.setHeader("Cache-Control", "no-cache");
            httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
            httpServletResponse.setContentType("application/json");
            OutputStream stream = httpServletResponse.getOutputStream();

            Iterator<MarshalledMessage> iter = messages.iterator();

            stream.write('[');
            while (iter.hasNext()) {
                writeToOutputStream(stream, iter.next());
                if (iter.hasNext()) {
                    stream.write(',');
                }
            }
            stream.write(']');

            stream.close();


        }
        catch (final Throwable t) {
            t.printStackTrace();
            httpServletResponse.setHeader("Cache-Control", "no-cache");
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
            stream.close();
        }
    }

}