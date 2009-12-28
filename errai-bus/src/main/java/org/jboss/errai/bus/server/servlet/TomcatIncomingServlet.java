package org.jboss.errai.bus.server.servlet;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.server.MessageQueue;
import org.jboss.errai.bus.server.QueueActivationCallback;
import org.jboss.errai.bus.server.io.MessageUtil;
import org.jboss.errai.bus.server.service.ErraiService;
import org.mvel2.util.StringAppender;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.List;

@Singleton
public class TomcatIncomingServlet extends HttpServlet {
    private ErraiService service;

    @Inject
    public TomcatIncomingServlet(ErraiService service) {
        this.service = service;
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        BufferedReader reader = httpServletRequest.getReader();
        StringAppender sb = new StringAppender(httpServletRequest.getContentLength());
        HttpSession session = httpServletRequest.getSession();
        CharBuffer buffer = CharBuffer.allocate(10);

        int read;
        while ((read = reader.read(buffer)) > 0) {
            buffer.rewind();
            for (; read > 0; read--) {
                sb.append(buffer.get());
            }
            buffer.rewind();
        }

        if (session.getAttribute(MessageBus.WS_SESSION_ID) == null) {
            session.setAttribute(MessageBus.WS_SESSION_ID, httpServletRequest.getSession().getId());
        }

        for (CommandMessage msg : MessageUtil.createCommandMessage(httpServletRequest.getSession(), sb.toString())) {
            service.store(msg);
        }

        pollForMessages(httpServletRequest, httpServletResponse, false);
    }


    private void pollForMessages(final HttpServletRequest httpServletRequest,
                                 final HttpServletResponse httpServletResponse, boolean wait) throws IOException {
        try {

            final MessageQueue queue = service.getBus().getQueue(httpServletRequest.getSession().getAttribute(MessageBus.WS_SESSION_ID));
            if (queue == null) return;

            if (wait) {
                synchronized (queue) {
                    queue.setActivationCallback(new QueueActivationCallback() {
                        public void activate() {
                            try {
                                transmitMessages(httpServletRequest, httpServletResponse, queue);
                            }
                            catch (IOException e) {
                                //todo: figure out a way to more gracefully handle this.
                                e.printStackTrace();
                            }
                        }
                    }
                    );

                }
            }

            transmitMessages(httpServletRequest, httpServletResponse, queue);
        }
        catch (final Throwable t) {
            t.printStackTrace();

            httpServletResponse.setHeader("Cache-Control", "no-cache");
            httpServletResponse.addHeader("Payload-Size", "1");
            httpServletResponse.setContentType("application/io");
            OutputStream stream = httpServletResponse.getOutputStream();

            stream.write('[');

            writeToOutputStream(stream, new Message() {
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
            stream.flush();
        }
    }

    public void transmitMessages(HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse, MessageQueue queue) throws IOException {

        List<Message> messages = queue.poll(false).getMessages();

        httpServletResponse.setHeader("Cache-Control", "no-cache");
        httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
        httpServletResponse.setContentType("application/io");
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

        queue.heartBeat();
    }

    public static void writeToOutputStream(OutputStream stream, Message m) throws IOException {
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
