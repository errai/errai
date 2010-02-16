/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
import org.jboss.errai.bus.client.MarshalledMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.server.MessageQueue;
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
        pollForMessages(httpServletRequest, httpServletResponse, true);
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
        try {
            ServletInputStream inputStream = httpServletRequest.getInputStream();
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(inputStream, "UTF-8")
            );
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

            for (Message msg : createCommandMessage(sessionProvider.getSession(httpServletRequest.getSession()), sb.toString())) {
                service.store(msg);
            }
        }
        catch (Throwable e) {
            // handle gracefully
            System.out.println("Error: https://jira.jboss.org/jira/browse/ERRAI-37");
            e.printStackTrace();
            httpServletResponse.setStatus(503); // Service Unavailable
        }

        pollForMessages(httpServletRequest, httpServletResponse, false);
    }

    private void pollForMessages(HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse, boolean wait) throws IOException {
        try {

            final MessageQueue queue = service.getBus().getQueue(httpServletRequest.getSession().getId());

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

    /**
     * Writes the message to the output stream
     *
     * @param stream - the stream to write to
     * @param m      - the message to write to the stream
     * @throws IOException - is thrown if any input/output errors occur while writing to the stream
     */
    public static void writeToOutputStream(OutputStream stream, MarshalledMessage m) throws IOException {
        stream.write('{');
        stream.write('"');
        for (byte b : (m.getSubject()).getBytes()) {
            stream.write(b);
        }
        stream.write('"');
        stream.write(':');

        if (m.getMessage() == null) {
            stream.write('n');
            stream.write('u');
            stream.write('l');
            stream.write('l');
        } else {
            for (byte b : ((String) m.getMessage()).getBytes()) {
                stream.write(b);
            }
        }
        stream.write('}');
    }
}
