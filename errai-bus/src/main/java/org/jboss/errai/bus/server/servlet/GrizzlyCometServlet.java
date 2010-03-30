package org.jboss.errai.bus.server.servlet;

import com.google.inject.Singleton;

import com.sun.grizzly.comet.CometContext;
import com.sun.grizzly.comet.CometEngine;
import com.sun.grizzly.comet.CometEvent;
import com.sun.grizzly.comet.handlers.ReflectorCometHandler;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.server.MessageQueue;
import org.mvel2.util.StringAppender;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.CharBuffer;
import java.util.*;

import static org.jboss.errai.bus.server.io.MessageFactory.createCommandMessage;

/**
 * The <tt>GrizzlyCometServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Glassfish.
 */
@Singleton
public class GrizzlyCometServlet extends AbstractErraiServlet {

    private static CometContext context = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    private static CometContext createCometContext(String id) {
        CometEngine cometEngine = CometEngine.getEngine();
        CometContext ctx = cometEngine.register(id);
        ctx.setExpirationDelay(-1);
        return ctx;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        pollForMessages(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        BufferedReader reader = request.getReader();
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

        for (Message msg : createCommandMessage(sessionProvider.getSession(request.getSession()), sb.toString())) {
            service.store(msg);
        }

        pollQueue(service.getBus().getQueue(request.getSession().getId()), request, response);
    }

    private void pollForMessages(HttpServletRequest httpServletRequest,
                                 HttpServletResponse httpServletResponse) throws IOException {

        try {
            final MessageQueue queue = service.getBus().getQueue(httpServletRequest.getSession().getId());

            if (queue == null) {
                sendDisconnectWithReason(httpServletResponse.getOutputStream(),
                        "There is no queue associated with this session.");
            }

            synchronized (queue) {
                pollQueue(queue, httpServletRequest, httpServletResponse);

                if (context == null)
                    context = createCometContext(httpServletRequest.getSession().getId());

                ReflectorCometHandler handler = new ReflectorCometHandler(true);
                context.addCometHandler(handler);

                context.notify(null, CometEvent.NOTIFY, handler);
            }
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

        queue.heartBeat();

        List<MarshalledMessage> messages = queue.poll(false).getMessages();

        httpServletResponse.setHeader("Cache-Control", "no-cache");
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
}
