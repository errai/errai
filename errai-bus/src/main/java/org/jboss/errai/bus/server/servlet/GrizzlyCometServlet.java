package org.jboss.errai.bus.server.servlet;

import com.google.inject.Singleton;

import com.sun.grizzly.arp.AsyncExecutor;
import com.sun.grizzly.arp.AsyncFilter;
import com.sun.grizzly.arp.AsyncHandler;
import com.sun.grizzly.arp.AsyncTask;
import com.sun.grizzly.comet.*;
import com.sun.grizzly.http.ProcessorTask;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.server.MessageQueue;
import org.mvel2.util.StringAppender;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
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

    public void init(ServletConfig config) throws ServletException {
        //    super.init(config);

        System.out.println("init !!!!!!!!!!!!");

        ServletContext context = config.getServletContext();
        contextPath = context.getContextPath() + "/in.erraiBus";
        CometEngine engine = CometEngine.getEngine();

        try {
            Method m = CometEngine.class.getDeclaredMethod("isCometEnabled", new Class[0]);
            m.setAccessible(true);
            Boolean bool = (Boolean) m.invoke(CometEngine.getEngine());

            if (!bool) {
                Field f = CometEngine.class.getDeclaredField("isCometSupported");
                f.setAccessible(true);
                f.set(null, true);
            }

            System.out.println("Is enabled: " + bool);

        }
        catch (Throwable t) {
            t.printStackTrace();
        }

        CometContext cometContext = engine.register(contextPath);
        cometContext.setExpirationDelay(120 * 1000);

        // HACK
        //CometAsyncFilter caf = new CometAsyncFilter();
        //caf.doFilter(new AsyncExecutorHandler());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        GrizzlyCometHandler handler = new GrizzlyCometHandler();
        handler.attach(resp);

        CometEngine engine = CometEngine.getEngine();
        CometContext context = engine.getCometContext(contextPath);

        context.addCometHandler(handler);

        if (req.getMethod().equals("POST")) {
            System.out.println("POST");
        } else {
            System.out.println("GET");
        }
    }

//    @Override
//    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
//            throws ServletException, IOException {
//        System.out.println("doGet !!!!!!!!!!!!");
//
////        try {
////            final MessageQueue queue = service.getBus().getQueue(httpServletRequest.getSession().getId());
////
////            if (queue == null)
////                sendDisconnectWithReason(httpServletResponse.getOutputStream(),
////                        "There is no queue associated with this session.");
////
////            synchronized (queue) {
////                pollQueue(queue, httpServletRequest, httpServletResponse);
////            }
////        }
////        catch (final Throwable t) {
////            t.printStackTrace();
////
////            httpServletResponse.setHeader("Cache-Control", "no-cache");
////            httpServletResponse.addHeader("Payload-Size", "1");
////            httpServletResponse.setContentType("application/json");
////            OutputStream stream = httpServletResponse.getOutputStream();
////
////            stream.write('[');
////
////            writeToOutputStream(stream, new MarshalledMessage() {
////                public String getSubject() {
////                    return "ClientBusErrors";
////                }
////
////                public Object getMessage() {
////                    StringBuilder b = new StringBuilder("{ErrorMessage:\"").append(t.getMessage()).append("\",AdditionalDetails:\"");
////                    for (StackTraceElement e : t.getStackTrace()) {
////                        b.append(e.toString()).append("<br/>");
////                    }
////
////                    return b.append("\"}").toString();
////                }
////            });
////
////            stream.write(']');
////        }
//    }
//
//    @Override
//    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
//            throws ServletException, IOException {
//        System.out.println("doPost !!!!!!!!!!!!");
//
//        CometEngine engine = CometEngine.getEngine();
//        CometContext<?> context = engine.getCometContext(contextPath);
//
//        context.notify(null);
//
////        BufferedReader reader = httpServletRequest.getReader();
////        StringAppender sb = new StringAppender(httpServletRequest.getContentLength());
////        CharBuffer buffer = CharBuffer.allocate(10);
////
////        int read;
////        while ((read = reader.read(buffer)) > 0) {
////            buffer.rewind();
////            for (; read > 0; read--) {
////                sb.append(buffer.get());
////            }
////            buffer.rewind();
////        }
////
////        for (Message msg : createCommandMessage(sessionProvider.getSession(httpServletRequest.getSession()), sb.toString())) {
////            service.store(msg);
////        }
////
////        pollQueue(service.getBus().getQueue(httpServletRequest.getSession().getId()), httpServletRequest, httpServletResponse);
//    }

    private static void pollQueue(MessageQueue queue, HttpServletRequest httpServletRequest,
                                  HttpServletResponse httpServletResponse) throws IOException {

//        queue.heartBeat();
//
//        List<MarshalledMessage> messages = queue.poll(false).getMessages();
//
//        httpServletResponse.setHeader("Cache-Control", "no-cache");
//        //    httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
//        httpServletResponse.setContentType("application/json");
//        OutputStream stream = httpServletResponse.getOutputStream();
//
//        Iterator<MarshalledMessage> iter = messages.iterator();
//
//        stream.write('[');
//        while (iter.hasNext()) {
//            writeToOutputStream(stream, iter.next());
//            if (iter.hasNext()) {
//                stream.write(',');
//            }
//        }
//        stream.write(']');
//        stream.flush();
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
        }

        public void attach(HttpServletResponse attachment) {
            System.out.println("attach !!!!!!!!!!!!");

            this.response = attachment;
        }

        public void onEvent(CometEvent event) throws IOException {
            System.out.println("onEvent !!!!!!!!!!!!");

//            if (CometEvent.NOTIFY == event.getType()) {
//                int count = 5;
//                PrintWriter writer = response.getWriter();
//                writer.write("<script type='text/javascript'>" +
//                        "parent.counter.updateCount('" + count + "')" +
//                        "</script>\n");
//                writer.flush();
//                event.getCometContext().resumeCometHandler(this);
//            }
        }
    }
}