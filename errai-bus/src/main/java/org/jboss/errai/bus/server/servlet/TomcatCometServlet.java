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
import org.apache.catalina.CometEvent;
import org.apache.catalina.CometProcessor;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueActivationCallback;
import org.jboss.errai.bus.server.api.QueueSession;
import org.mvel2.util.StringAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.jboss.errai.bus.server.io.MessageFactory.createCommandMessage;

/**
 * The <tt>TomcatCometServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Tomcat Comet.
 */
@Singleton
public class TomcatCometServlet extends AbstractErraiServlet implements CometProcessor {
    private volatile ClassLoader contextClassLoader;

    public TomcatCometServlet() {
    }

    private final Map<MessageQueue, QueueSession> queueToSession = new ConcurrentHashMap<MessageQueue, QueueSession>();
    private final Map<QueueSession, Set<CometEvent>> activeEvents = new ConcurrentHashMap<QueueSession, Set<CometEvent>>();

    private Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * When an event is received, it is processed accordingly. A post event will tell the servlet to wait for the
     * messages, if there are messages waiting, they will be transmitted. Any errors will be handled.
     *
     * @param event - the Http event that occured
     * @throws IOException      - thrown if there is a read/write error
     * @throws ServletException - thrown if a servlet error occurs
     */
    public void event(final CometEvent event) throws IOException, ServletException {
        final HttpServletRequest request = event.getHttpServletRequest();
        final QueueSession session = sessionProvider.getSession(request.getSession(), request.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

        MessageQueue queue;
        switch (event.getEventType()) {
            case BEGIN:
                boolean post = "POST".equals(request.getMethod());
                if ((queue = getQueue(session, !post)) != null) {
                    synchronized (activeEvents) {
                        Set<CometEvent> events = activeEvents.get(session);


                        if (post) {
                            // do not pause incoming messages.
                            break;
                        } else if (queue.messagesWaiting()) {
                            transmitMessages(event.getHttpServletResponse(), queue);
                            event.close();
                            break;
                        }

                        if (!queueToSession.containsKey(queue)) {
                            queueToSession.put(queue, session);
                        }

                        if (events == null) {
                            activeEvents.put(session, events = new HashSet<CometEvent>());
                        }
                        if (events.contains(event)) {
                            break;
                        } else {
                            event.setTimeout(30000);
                            events.add(event);
                        }
                    }
                } else {
                    switch (getConnectionPhase(request)) {
                        case CONNECTING:
                        case DISCONNECTING:
                            return;
                    }

                    sendDisconnectWithReason(event.getHttpServletResponse().getOutputStream(),
                            "There is no queue associated with this session.");
                }
                break;

            case END:
                event.close();
                synchronized (activeEvents) {
                    Set<CometEvent> evt = activeEvents.get(session);
                    if (evt != null && !evt.remove(event)) {
                        return;
                    }
                }

                if ((queue = getQueue(session, false)) != null) {
                    queue.heartBeat();
                } else {
                    return;
                }


                break;

            case ERROR:
                log.error("An Error Occured: " + (event != null ? event.getEventSubType() : "<null>"));

                queue = getQueue(session, false);
                if (queue == null) {
                    return;
                }

                synchronized (activeEvents) {
                    Set<CometEvent> evt = activeEvents.get(session);
                    if (evt != null && !evt.remove(event)) {
                        return;
                    }
                }

                if (event.getEventSubType() == CometEvent.EventSubType.TIMEOUT) {
                    if (queue != null) queue.heartBeat();
                } else {
                    if (queue != null) {
                        queueToSession.remove(queue);
                        service.getBus().closeQueue(session.getSessionId());
                        activeEvents.remove(session);
                    }

                }

                event.close();
                break;

            case READ:
                readInRequest(session, request);
                event.close();
        }
    }

    /**
     * Receives standard HTTP requests from the public, and writes it to the response's output stream in JSON format
     *
     * @param req                 - the object that contains the request the client made of the servlet
     * @param httpServletResponse - the object that contains the response the servlet returns to the client
     * @throws IOException      - if an input or output error occurs while the servlet is handling the HTTP request
     * @throws ServletException - if the HTTP request cannot be handled
     */
    @Override
    protected void service(HttpServletRequest req, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        System.out.println(CONFIG_PROBLEM_TEXT);
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
                StringBuilder b = new StringBuilder("{ErrorMessage:\"").append(CONFIG_PROBLEM_TEXT).append("\",AdditionalDetails:\"");
                return b.append("\"}").toString();
            }
        });

        stream.write(',');

        writeToOutputStream(stream, new MarshalledMessage() {
            public String getSubject() {
                return "ClientBus";
            }

            public Object getMessage() {
                return "{CommandType:\"Disconnect\"}";
            }
        });


        stream.write(']');
    }

    private int readInRequest(QueueSession session, HttpServletRequest request) {
        try {

            BufferedReader reader = request.getReader();
            if (!reader.ready()) return 0;
            StringAppender sb = new StringAppender(request.getContentLength());
            CharBuffer buffer = CharBuffer.allocate(10);
            int read;
            while ((read = reader.read(buffer)) > 0) {
                buffer.rewind();
                for (; read > 0; read--) {
                    sb.append(buffer.get());
                }
                buffer.rewind();
            }


            Message msg = createCommandMessage(sessionProvider.getSession(request.getSession(),
                    request.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER)), sb.toString());
            if (msg != null) {
                service.store(msg);
                return 1;
            } else {
                return 0;
            }
        } catch (IOException e) {
            MessageQueue queue = service.getBus().getQueue(session);
            if (queue != null) {
                queue.stopQueue();
            }
            e.printStackTrace();
            return -1;
        }
    }


    private MessageQueue getQueue(QueueSession session, boolean pause) {
        MessageQueue queue = service.getBus().getQueue(session);
        if (pause && queue != null && queue.getActivationCallback() == null) {
            queue.setActivationCallback(new QueueActivationCallback() {
                volatile boolean resumed = false;

                public void activate(MessageQueue queue) {
                    //      log.info("Resume...");
                    if (resumed) {
                        return;
                    }
                    resumed = true;
                    queue.setActivationCallback(null);

                    try {
                        Set<CometEvent> activeSessEvents;
                        QueueSession session;
                        session = queueToSession.get(queue);
                        if (session == null) {
                            log.error("Could not resume: No session.");
                            queue.stopQueue();
                            return;
                        }

                        activeSessEvents = activeEvents.get(queueToSession.get(queue));

                        if (activeSessEvents == null || activeSessEvents.isEmpty()) {
                            //        log.error("Nothing active man");
                            return;
                        }

                        CometEvent et = activeSessEvents.iterator().next();

                        if (et == null) {
                            return;
                        }

                        try {
                            transmitMessages(et.getHttpServletResponse(), queue);
                        } catch (NullPointerException e) {
                            activeSessEvents.remove(et);
                            return;
                        }

                        try {
                            et.close();
                        } catch (NullPointerException e) {
                            // suppress.
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return queue;
    }

    /**
     * Transmits messages from the queue to the response, by writing them to the response's output stream in JSON
     * format
     *
     * @param httpServletResponse - the response that will contain all the messages to be transmitted
     * @param queue               - the queue holding the messages to be transmitted
     * @throws IOException - if an input or output error occurs while the servlet is handling the HTTP request
     */
    public void transmitMessages(final HttpServletResponse httpServletResponse, MessageQueue queue) throws IOException {
        //  log.info("Transmitting messages to client (Queue:" + queue.hashCode() + ")");
        httpServletResponse.setHeader("Cache-Control", "no-cache");
        //    httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
        httpServletResponse.setContentType("application/json");
        queue.poll(false, httpServletResponse.getOutputStream());
        queue.heartBeat();
    }


    private static final String CONFIG_PROBLEM_TEXT =
            "\n\n*************************************************************************************************\n"
                    + "** PROBLEM!\n"
                    + "** It appears something has been incorrectly configured. In order to use ErraiBus\n"
                    + "** on Tomcat, you must ensure that you are using the NIO or APR connector. Also \n"
                    + "** make sure that you have added these lines to your WEB-INF/web.xml file:\n"
                    + "**                                              ---\n"
                    + "**    <servlet>\n" +
                    "**        <servlet-name>TomcatErraiServlet</servlet-name>\n" +
                    "**        <servlet-class>org.jboss.errai.bus.server.servlet.TomcatCometServlet</servlet-class>\n" +
                    "**        <load-on-startup>1</load-on-startup>\n" +
                    "**    </servlet>\n" +
                    "**\n" +
                    "**    <servlet-mapping>\n" +
                    "**        <servlet-name>TomcatErraiServlet</servlet-name>\n" +
                    "**        <url-pattern>*.erraiBus</url-pattern>\n" +
                    "**    </servlet-mapping>\n"
                    + "**                                              ---\n"
                    + "** If you have the following lines in your WEB-INF/web.xml, you must comment or remove them:\n"
                    + "**                                              ---\n"
                    + "**    <listener>\n" +
                    "**        <listener-class>org.jboss.errai.bus.server.ErraiServletConfig</listener-class>\n" +
                    "**    </listener>\n"
                    + "*************************************************************************************************\n\n";
}
