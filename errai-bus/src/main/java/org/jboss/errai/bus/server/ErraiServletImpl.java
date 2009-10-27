package org.jboss.errai.bus.server;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.Message;
import org.jboss.errai.bus.client.MessageBus;
import org.jboss.errai.bus.client.protocols.MessageParts;
import org.jboss.errai.bus.client.protocols.SecurityParts;
import org.jboss.errai.bus.server.io.MessageUtil;
import static org.jboss.errai.bus.server.io.MessageUtil.createCommandMessage;
import static org.jboss.errai.bus.server.io.MessageUtil.decodeToMap;
import org.jboss.errai.bus.server.service.ErraiService;

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
import java.util.Map;

/**
 * The main gateway of the Workspace application to the server.  All communication between the client and the
 * server passes through this class.
 */
@Singleton
public class ErraiServletImpl extends HttpServlet {
    private ErraiService service;

    @Inject
    public ErraiServletImpl(ErraiService service) {
        this.service = service;
    }

    @Override
    protected void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {

        List<Message> messages = service.getBus().nextMessage(
                httpServletRequest.getSession().getAttribute(MessageBus.WS_SESSION_ID)).getMessages();

        httpServletResponse.setHeader("Cache-Control", "no-cache");
        httpServletResponse.addHeader("Payload-Size", String.valueOf(messages.size()));
        httpServletResponse.setContentType("application/io");
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

            if (m.getMessage() == null) {
                stream.write('n');
                stream.write('u');
                stream.write('l');
                stream.write('l');
            }
            else {
                for (byte b : ((String) m.getMessage()).getBytes()) {
                    stream.write(b);
                }
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
    protected void doPost(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws ServletException, IOException {

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

        for (CommandMessage msg : createCommandMessage(httpServletRequest.getSession(), sb.toString())) {
            service.store(msg);
        }

        OutputStream stream = httpServletResponse.getOutputStream();

        stream.write('<');
        stream.write('n');
        stream.write('u');
        stream.write('l');
        stream.write('l');
        stream.write('>');
        stream.write('<');
        stream.write('/');
        stream.write('n');
        stream.write('u');
        stream.write('l');
        stream.write('l');
        stream.write('>');
        stream.close();
    }
}
