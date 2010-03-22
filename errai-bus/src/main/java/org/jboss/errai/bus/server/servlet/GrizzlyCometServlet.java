package org.jboss.errai.bus.server.servlet;

import com.google.inject.Singleton;

import com.sun.grizzly.comet.*;
import com.sun.grizzly.comet.handlers.ReflectorCometHandler;
import org.jboss.errai.bus.client.framework.MarshalledMessage;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The <tt>GrizzlyCometServlet</tt> provides the HTTP-protocol gateway between the server bus and the client buses,
 * using Glassfish.
 */
@Singleton
public class GrizzlyCometServlet extends AbstractErraiServlet {

    private ServletContext servletContext = null;
    private CometContext context = null;

    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        servletContext = config.getServletContext();
    }

    private CometContext createCometContext(String id) {
        CometEngine cometEngine = CometEngine.getEngine();
        CometContext ctx = cometEngine.register(id);
        ctx.setExpirationDelay(90 * 1000);
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
        System.out.println("doPost !!!!!!!!!!!!");

        String sessionId = request.getSession().getId();
        HttpSession session = request.getSession();

        if (context != null) {
            context = (CometContext) session.getAttribute(sessionId);
        } else {
            context = createCometContext(sessionId);

            GrizzlyCometHandler handler = new GrizzlyCometHandler();

            handler.attach(response.getWriter());
            context.addCometHandler(handler);

            context.addAttribute("handler", handler);
            session.setAttribute("handler", handler);
            session.setAttribute(sessionId, context);
        }

        System.out.println(request.getMethod() + "*********");

        // do post, get, init, error/end

        CometHandler ch = (CometHandler) session.getAttribute("handler");
        context.notify("hello!", CometEvent.NOTIFY, ch);
    }

    private class GrizzlyCometHandler extends ReflectorCometHandler {

        @Override
        public synchronized void onInitialize(CometEvent event) throws IOException {
            System.out.println("onInit !!!!!!!!!!!");

            // BEGIN
        }

        @Override
        public synchronized void onEvent(CometEvent event) throws IOException {
            System.out.println("onEvent !!!!!!!!!!!! " + event.getType());

            // POST/GET
        }

        @Override
        public synchronized void onInterrupt(CometEvent cometEvent) throws IOException {
            System.out.println("onInterr !!!!!!!!!!!");

            //END/ERROR
        }
    }
}