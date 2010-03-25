package org.jboss.errai.bus.server.servlet;

import com.google.inject.Singleton;

import com.sun.grizzly.comet.CometContext;
import com.sun.grizzly.comet.CometEngine;
import com.sun.grizzly.comet.CometEvent;
import com.sun.grizzly.comet.handlers.ReflectorCometHandler;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.server.MessageQueue;
import org.jboss.errai.bus.server.QueueActivationCallback;
import org.jboss.errai.bus.server.QueueSession;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.*;

/**
 * The <tt>GrizzlyCometServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Glassfish.
 */
@Singleton
public class GrizzlyCometServlet extends AbstractErraiServlet implements Serializable {

    private static final long serialVersionUID = -2919167206889576861L;

    private CometContext context = null;
    private final Map<MessageQueue, QueueSession> queueToSession = new HashMap<MessageQueue, QueueSession>();
    private final Map<QueueSession, Set<HttpServletResponse>> activeEvents = new HashMap<QueueSession, Set<HttpServletResponse>>();
    private ReflectorCometHandler handler = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);    
    }

    private CometContext createCometContext(String id) {
        CometEngine cometEngine = CometEngine.getEngine();
        CometContext ctx = cometEngine.register(id);
        ctx.setExpirationDelay(-1);
        return ctx;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        doPost(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String sessionId = request.getSession().getId();
        final QueueSession session = sessionProvider.getSession(request.getSession());
        PrintWriter attach = response.getWriter();

        if (context == null) {

            context = createCometContext(sessionId);

            handler = new ReflectorCometHandler(true, buildStartingMessage(response), "");

            handler.attach(attach);
            context.addCometHandler(handler);
        } else {

            boolean post = "POST".equals(request.getMethod());

            MessageQueue queue = getQueue(session, !post);

            if (queue != null) {
                synchronized (activeEvents) {
                    Set<HttpServletResponse> responses = activeEvents.get(session);

                    if (!post && queue.messagesWaiting()) {
                        context.notify(getMessages(response, queue), CometEvent.NOTIFY, handler);
                    }

                    if (!queueToSession.containsKey(queue))
                        queueToSession.put(queue, session);

                    if (responses == null)
                        activeEvents.put(session, responses = new HashSet<HttpServletResponse>());

                    if (!responses.contains(response))
                        responses.add(response);
                }
            } else {
                // FIXME!!!!!!!!!!!!!!!
                // SEND ERROR
            }
        }
    }

    private String getMessages(final HttpServletResponse httpServletResponse, MessageQueue queue) throws IOException {
        httpServletResponse.setHeader("Cache-Control", "no-cache");
        //    httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
        httpServletResponse.setContentType("application/json");
        StringBuilder sb = new StringBuilder();

        List<MarshalledMessage> messages = queue.poll(false).getMessages();
        Iterator<MarshalledMessage> iter = messages.iterator();

        sb.append('[');
        while (iter.hasNext()) {
            writeToOutputStream(sb, iter.next());
            if (iter.hasNext()) {
                sb.append(',');
            }
        }
        sb.append(']');

        queue.heartBeat();

        return sb.toString();
    }


    /**
     * Before suspending message
     *
     * @return
     */
    private String buildStartingMessage(HttpServletResponse response) throws IOException {
        response.setHeader("Cache-Control", "no-cache");
        response.addHeader("Payload-Size", "1");
        response.setContentType("application/json");

        StringBuilder sb = new StringBuilder();

        sb.append('[');

        writeToOutputStream(sb, new MarshalledMessage() {
            public String getSubject() {
                return "ClientBusErrors";
            }

            public Object getMessage() {
                StringBuilder b = new StringBuilder("{ErrorMessage:\"").append(CONFIG_PROBLEM_TEXT).append("\",AdditionalDetails:\"");
                return b.append("\"}").toString();
            }
        });

        sb.append(',');

        writeToOutputStream(sb, new MarshalledMessage() {
            public String getSubject() {
                return "ClientBus";
            }

            public Object getMessage() {
                return "{CommandType:\"Disconnect\"}";
            }
        });


        sb.append(']');

        return sb.toString();
    }

    /**
     * Writes the message to the output stream
     *
     * @param sb - the string builder to write to
     * @param m  - the message to write to the stream
     * @throws java.io.IOException - is thrown if any input/output errors occur while writing to the stream
     */
    private void writeToOutputStream(StringBuilder sb, MarshalledMessage m) throws IOException {
        sb.append('{');
        sb.append('"');
        for (byte b : (m.getSubject()).getBytes()) {
            sb.append(b);
        }
        sb.append('"');
        sb.append(':');

        if (m.getMessage() == null) {
            sb.append('n');
            sb.append('u');
            sb.append('l');
            sb.append('l');
        } else {
            for (byte b : ((String) m.getMessage()).getBytes()) {
                sb.append(b);
            }
        }
        sb.append('}');
    }

    private MessageQueue getQueue(QueueSession session, boolean pause) {

        System.out.println("getQueue !!!!!! " + service  + " " +  service.getBus() + " " +
                session.getSessionId() + " " + service.getBus().getQueue(session.getSessionId()));

        MessageQueue queue = service.getBus().getQueue(session.getSessionId());

        System.out.println("getQueue !!!!!! " + pause + ":pause, queue is " + queue == null);
        
        if (pause && queue != null && queue.getActivationCallback() == null) {

            System.out.println("getQueue !!!!!! in first if");

            queue.setActivationCallback(new QueueActivationCallback() {
                volatile boolean resumed = false;

                public void activate(MessageQueue queue) {
                    System.out.println("getQueue !!!!!! activate");

                    if (resumed) {
                        return;
                    }
                    resumed = true;
                    queue.setActivationCallback(null);

                    try {

                        System.out.println("getQueue !!!!!! first try block");

                        Set<HttpServletResponse> activeSessEvents;
                        QueueSession session;
                        session = queueToSession.get(queue);
                        if (session == null) {
                            System.out.println("getQueue !!!!!! session is null");

                            queue.stopQueue();
                            return;
                        }

                        activeSessEvents = activeEvents.get(queueToSession.get(queue));

                        if (activeSessEvents == null || activeSessEvents.isEmpty()) {
                            return;
                        }

                        HttpServletResponse hsr = activeSessEvents.iterator().next();

                        if (hsr == null) {
                            return;
                        }

                        try {

                            System.out.println("getQueue !!!!!! second try block");
                            context.notify(getMessages(hsr, queue), CometEvent.NOTIFY, handler);
                        }
                        catch (NullPointerException e) {

                            System.out.println("getQueue !!!!!! NPE caught");

                            activeSessEvents.remove(hsr);
                            return;
                        }
                    }
                    catch (Exception e) {

                        System.out.println("getQueue !!!!!! exception caught" + e);

                        e.printStackTrace();
                    }
                }
            });
        }

        return queue;
    }

    private static final String CONFIG_PROBLEM_TEXT =
            "\n\n*************************************************************************************************\n"
                    + "** PROBLEM!\n"
                    + "** It appears something has been incorrectly configured. In order to use ErraiBus\n"
                    + "** on Glassfish, you must ensure that you are using the NIO or APR connector. Also \n"
                    + "** make sure that you have added these lines to your WEB-INF/web.xml file:\n"
                    + "**                                              ---\n"
                    + "**    <servlet>\n" +
                    "**        <servlet-name>GrizzlyErraiServlet</servlet-name>\n" +
                    "**        <servlet-class>org.jboss.errai.bus.server.servlet.GrizzlyCometServlet</servlet-class>\n" +
                    "**        <load-on-startup>1</load-on-startup>\n" +
                    "**    </servlet>\n" +
                    "**\n" +
                    "**    <servlet-mapping>\n" +
                    "**        <servlet-name>GrizzlyErraiServlet</servlet-name>\n" +
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