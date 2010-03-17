package org.jboss.errai.bus.server.servlet;

import com.google.inject.Singleton;

import com.sun.grizzly.comet.*;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.server.MessageQueue;
import org.mvel2.util.StringAppender;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.List;

import static org.jboss.errai.bus.server.io.MessageFactory.createCommandMessage;

/**
 * The <tt>GrizzlyCometServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Glassfish.
 */
@Singleton
public class GrizzlyCometServlet extends AbstractErraiServlet {

    private String contextPath = null;
    private CometContext cometContext = null;
    private GrizzlyCometHandler handler = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        System.out.println("init !!!!!!!!!!!!");

        contextPath = config.getServletContext().getContextPath() + "/in.erraiBus";
        cometContext = CometEngine.getEngine().register(contextPath);
        handler = new GrizzlyCometHandler();
        cometContext.addCometHandler(handler);
        cometContext.setExpirationDelay(30 * 1000);
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        System.out.println("doGet !!!!!!!!!!!!");

        handler.attach(httpServletResponse);

        try {
            final MessageQueue queue = service.getBus().getQueue(httpServletRequest.getSession().getId());

            if (queue == null)
                sendDisconnectWithReason(httpServletResponse.getOutputStream(),
                        "There is no queue associated with this session.");

            synchronized (queue) {
                pollQueue(queue, httpServletRequest, httpServletResponse);
            }

            cometContext.notify("doGet", CometEvent.NOTIFY);
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
        }
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {
        System.out.println("doPost !!!!!!!!!!!!");

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

        for (Message msg : createCommandMessage(sessionProvider.getSession(httpServletRequest.getSession()), sb.toString())) {
            service.store(msg);
        }

        try {
            final MessageQueue queue = service.getBus().getQueue(httpServletRequest.getSession().getId());

            if (queue == null)
                sendDisconnectWithReason(httpServletResponse.getOutputStream(),
                        "There is no queue associated with this session.");

            synchronized (queue) {
                pollQueue(queue, httpServletRequest, httpServletResponse);
            }

            cometContext.notify("doPost", CometEvent.NOTIFY);
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
        }
    }

    private static void pollQueue(MessageQueue queue, HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse) throws IOException {

        System.out.println("pollQueue !!!!!!!!!" + queue);
        queue.heartBeat();

        List<MarshalledMessage> messages = queue.poll(false).getMessages();

        httpServletResponse.setHeader("Cache-Control", "no-cache");
        //    httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
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
        stream.flush();
    }


    private class GrizzlyCometHandler implements CometHandler<HttpServletResponse> {
        private HttpServletResponse response;

        public void onInitialize(CometEvent event) throws IOException {
            System.out.println("onInitialize !!!!!!!!!!!!");
        }

        public void onInterrupt(CometEvent event) throws IOException {
            System.out.println("onInterr !!!!!!!!!!!!");
        }

        public void onTerminate(CometEvent event) throws IOException {
            System.out.println("onTerm !!!!!!!!!!!!");

            onInterrupt(event);
        }

        public void attach(HttpServletResponse attachment) {
            System.out.println("attach !!!!!!!!!!!!");

            this.response = attachment;
        }

        public void onEvent(CometEvent event) throws IOException {
            System.out.println("onEvent !!!!!!!!!!!!");
        }
    }
}