package org.jboss.errai.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.client.rpc.CommandMessage;
import org.jboss.errai.client.rpc.Message;
import org.jboss.errai.client.rpc.protocols.SecurityParts;
import org.jboss.errai.server.bus.MessageBus;
import org.jboss.errai.server.bus.Payload;
import static org.jboss.errai.server.json.JSONUtil.decodeToMap;
import org.jboss.errai.server.service.ErraiService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.CharBuffer;
import java.util.Iterator;
import java.util.List;

/**
 * The main gateway of the Workspace application to the server.  All communication between the client and the
 * server passes through this class.
 */
@Singleton
public class MessageBusServiceImpl extends HttpServlet {
    private ErraiService service;

    @Inject
    public MessageBusServiceImpl(ErraiService service) {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        Payload p = service.getBus().nextMessage(
                httpServletRequest.getSession().getAttribute(MessageBus.WS_SESSION_ID));

        List<Message> messages = p.getMessages();

        httpServletResponse.setHeader("Cache-Control", "no-cache");
        httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
        OutputStream stream = httpServletResponse.getOutputStream();

        Iterator<Message> iter = messages.iterator();
        Message m;

        stream.write('[');
        while (iter.hasNext()) {
            m = iter.next();
            stream.write('{');
            stream.write('"');
            for (byte b : (m.getSubject()).getBytes()) {
                stream.write(b);
            }
            stream.write('"');
            stream.write(':');

            for (byte b : ((String) m.getMessage()).getBytes()) {
                stream.write(b);
            }
            stream.write('}');

            if (iter.hasNext()) {
                stream.write(',');
            }
        }
        stream.write(']');

        stream.close();
    }

    @Override
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) throws ServletException, IOException {
        Reader reader = httpServletRequest.getReader();
        StringBuilder sb = new StringBuilder(httpServletRequest.getContentLength());
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

        service.store(new CommandMessage()
                .setParts(decodeToMap(sb.toString()))
                .set(SecurityParts.SessionData, httpServletRequest.getSession()));
    }


}
