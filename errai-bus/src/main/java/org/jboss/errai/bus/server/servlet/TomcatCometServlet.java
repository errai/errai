package org.jboss.errai.bus.server.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.catalina.CometEvent;
import org.apache.catalina.CometFilter;
import org.apache.catalina.CometProcessor;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.MessageQueue;
import org.jboss.errai.bus.server.QueueActivationCallback;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.mvel2.util.StringAppender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.*;

import static org.jboss.errai.bus.server.io.MessageUtil.createCommandMessage;

@Singleton
public class TomcatCometServlet extends HttpServlet implements CometProcessor {
    private ErraiService service;

    public TomcatCometServlet() {
        // bypass guice-servlet
        service = Guice.createInjector(new AbstractModule() {
            public void configure() {
                bind(MessageBus.class).to(ServerMessageBusImpl.class);
                bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
                bind(ErraiService.class).to(ErraiServiceImpl.class);
                bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
            }
        }).getInstance(ErraiService.class);
    }

    @Inject
    public TomcatCometServlet(ErraiService service) {
        this.service = service;

    }

    private final Map<MessageQueue, HttpSession> queueToSession = new HashMap<MessageQueue, HttpSession>();
    private final HashMap<HttpSession, Set<CometEvent>> activeEvents = new HashMap<HttpSession, Set<CometEvent>>();

    private Logger log = LoggerFactory.getLogger(this.getClass());

    public void event(final CometEvent event) throws IOException, ServletException {
        HttpServletRequest request = event.getHttpServletRequest();
        final HttpSession session = request.getSession();

        MessageQueue queue;
        switch (event.getEventType()) {
            case BEGIN:

                if (session.getAttribute(MessageBus.WS_SESSION_ID) == null) {
                    session.setAttribute(MessageBus.WS_SESSION_ID, session.getId());
                } else {
                    //  log.info("BEGIN:" + event.hashCode());
                    if ((queue = getQueue(session)) != null && queue.messagesWaiting()) {
                        transmitMessages(event.getHttpServletResponse(), queue);
                        event.close();
                        break;
                    }

                    synchronized (session) {
                        if (queue != null && !queueToSession.containsKey(queue)) {
                            queueToSession.put(queue, session);
                        }

                        if (!activeEvents.containsKey(session)) {
                            activeEvents.put(session, new LinkedHashSet<CometEvent>());
                        }

                        activeEvents.get(session).add(event);
                    }
                }
                break;

            case END:
                MessageQueue toRemove = getQueue(request.getSession());
                if (toRemove != null) toRemove.heartBeat();

                synchronized (session) {
                    activeEvents.get(session).remove(event);
                    //     log.info("Remove Active Event (ActiveInSession: " + activeEvents.get(request.getSession()).size() + ")");
                }
                event.getHttpServletResponse().flushBuffer();
                //     log.info("END:" + event.hashCode());
                event.close();

                break;

            case ERROR:
                //   log.error("An error occured: " + event.getEventSubType());
                synchronized (session) {
                    activeEvents.get(session).remove(event);
                    log.error(event.toString());
                    toRemove = getQueue(request.getSession());

                    queueToSession.remove(toRemove);
                }
                event.close();
                break;

            case READ:
                //    log.info("READ:" + event.hashCode());
                readInRequest(request);
                event.close();
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        System.out.println(CONFIG_PROBLEM_TEXT);
        httpServletResponse.setHeader("Cache-Control", "no-cache");
        httpServletResponse.addHeader("Payload-Size", "1");
        httpServletResponse.setContentType("application/json");
        OutputStream stream = httpServletResponse.getOutputStream();

        stream.write('[');

        writeToOutputStream(stream, new Message() {
            public String getSubject() {
                return "ClientBusErrors";
            }

            public Object getMessage() {
                StringBuilder b = new StringBuilder("{ErrorMessage:\"").append(CONFIG_PROBLEM_TEXT).append("\",AdditionalDetails:\"");
                return b.append("\"}").toString();
            }
        });

        stream.write(']');

    }

    private int readInRequest(HttpServletRequest request) throws IOException {
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

        //    log.info("ReceivedFromClient:" + sb.toString());

        int messagesSent = 0;
        for (CommandMessage msg : createCommandMessage(request.getSession(), sb.toString())) {
            service.store(msg);
            messagesSent++;
        }

        //   log.info("Messages stored into bus: " + messagesSent);

        return messagesSent;
    }


    private MessageQueue getQueue(HttpSession session) {
        // final HttpSession session = event.getHttpServletRequest().getSession();
        MessageQueue queue = service.getBus().getQueue(session.getAttribute(MessageBus.WS_SESSION_ID));

        if (queue != null && queue.getActivationCallback() == null) {
            queue.setActivationCallback(new QueueActivationCallback() {
                public void activate(MessageQueue queue) {
                    try {
                        Set<CometEvent> activeSessEvents;
                        HttpSession session = queueToSession.get(queue);
                        if (session == null) return;

                        synchronized (session) {
                            activeSessEvents = activeEvents.get(queueToSession.get(queue));

                            if (activeSessEvents == null) {
                                log.warn("No active events to resume with");
                                return;
                            }

                            Iterator<CometEvent> iter = activeSessEvents.iterator();
                            CometEvent et;
                            while (iter.hasNext()) {
                                try {
                                    transmitMessages((et = iter.next()).getHttpServletResponse(), queue);
                                    et.close();
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                    return;
                                }
                            }

                        }
                    }
                    catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
        }

        return queue;
    }


    public void transmitMessages(final HttpServletResponse httpServletResponse, MessageQueue queue) throws IOException {

        //   log.info("Transmitting messages to client (Queue:" + queue.hashCode() + ")");
        List<Message> messages = queue.poll(false).getMessages();
        httpServletResponse.setHeader("Cache-Control", "no-cache");
        httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
        httpServletResponse.setContentType("application/json");
        OutputStream stream = httpServletResponse.getOutputStream();

        Iterator<Message> iter = messages.iterator();

        stream.write('[');
        while (iter.hasNext()) {
            writeToOutputStream(stream, iter.next());
            if (iter.hasNext()) {
                stream.write(',');
            }
        }
        stream.write(']');
        stream.flush();
        //   queue.heartBeat();
    }

    public void writeToOutputStream(OutputStream stream, Message m) throws IOException {
        //    log.info("SendToClient:" + m.getMessage());

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

    private static final class PausedEvent {
        private HttpServletResponse response;
        private HttpSession session;
        private CometEvent event;

        private PausedEvent(HttpServletResponse response, HttpSession session, CometEvent event) {
            this.response = response;
            this.session = session;
            this.event = event;
        }

        public HttpServletResponse getResponse() {
            return response;
        }

        public void setResponse(HttpServletResponse response) {
            this.response = response;
        }

        public HttpSession getSession() {
            return session;
        }

        public void setSession(HttpSession session) {
            this.session = session;
        }

        public CometEvent getEvent() {
            return event;
        }

        public void setEvent(CometEvent event) {
            this.event = event;
        }
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
