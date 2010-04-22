package org.jboss.errai.bus.server.servlet;

import com.google.inject.Singleton;
import org.jboss.errai.bus.client.framework.ClientMessageBus;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.server.MessageQueue;
import org.jboss.errai.bus.server.QueueActivationCallback;
import org.jboss.errai.bus.server.QueueSession;
import org.mortbay.jetty.RetryRequest;
import org.mortbay.util.ajax.Continuation;
import org.mortbay.util.ajax.ContinuationSupport;
import org.mvel2.util.StringAppender;

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
 * The <tt>JettyContinuationsServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Jetty Continuations.
 */
@Singleton
public class JettyContinuationsServlet extends AbstractErraiServlet {

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

        final QueueSession session = sessionProvider.getSession(httpServletRequest.getSession(),
                httpServletRequest.getHeader(ClientMessageBus.REMOTE_QUEUE_ID_HEADER));

        for (Message msg : createCommandMessage(session, sb.toString())) {
            service.store(msg);
        }

        pollQueue(service.getBus().getQueue(session), httpServletRequest, httpServletResponse);
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

            synchronized (queue) {
                if (wait) {
                    final Continuation cont = ContinuationSupport.getContinuation(httpServletRequest, queue);

                    if (!queue.messagesWaiting()) {

                        queue.setActivationCallback(new QueueActivationCallback() {
                            public void activate(MessageQueue queue) {
                                queue.setActivationCallback(null);
                                cont.resume();
                            }
                        });

                        if (!queue.messagesWaiting()) {
                            cont.suspend(45 * 1000);
                        }
                    } else {
                        queue.setActivationCallback(null);
                    }

                }

                pollQueue(queue, httpServletRequest, httpServletResponse);
            }
        }
        catch (RetryRequest r) {
            /**
             * This *must* be caught and re-thrown to work property with Jetty.
             */

            throw r;
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
                    StringBuilder b = new StringBuilder("{Error" +
                            "Message:\"").append(t.getMessage()).append("\",AdditionalDetails:\"");
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

        if (queue == null) return;

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
        // stream.close();

    }


}
