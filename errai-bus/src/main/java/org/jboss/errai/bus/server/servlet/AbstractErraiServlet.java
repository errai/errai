package org.jboss.errai.bus.server.servlet;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import org.jboss.errai.bus.client.framework.MarshalledMessage;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.ServerMessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.SessionProvider;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.OutputStream;

/**
 * The <tt>AbstractErraiServlet</tt> provides a starting point for creating Http-protocol gateway between the server
 * bus and the client buses.
 */
public abstract class AbstractErraiServlet extends HttpServlet {
    /* New and configured errai service */
    protected ErraiService service =
            Guice.createInjector(new AbstractModule() {
                public void configure() {
                    bind(MessageBus.class).to(ServerMessageBusImpl.class);
                    bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
                    bind(ErraiService.class).to(ErraiServiceImpl.class);
                    bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
                }
            }).getInstance(ErraiService.class);

    
    /* A default Http session provider */
    @SuppressWarnings({"unchecked"})
    protected SessionProvider<HttpSession> sessionProvider = service.getConfiguration().getConfiguredSessionProvider();

    protected Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Writes the message to the output stream
     *
     * @param stream - the stream to write to
     * @param m      - the message to write to the stream
     * @throws java.io.IOException - is thrown if any input/output errors occur while writing to the stream
     */
    public static void writeToOutputStream(OutputStream stream, MarshalledMessage m) throws IOException {
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

    protected void sendDisconnectWithReason(OutputStream stream, final String reason) throws IOException {
        writeToOutputStream(stream, new MarshalledMessage() {
            public String getSubject() {
                return "ClientBus";
            }

            public Object getMessage() {
                return reason != null ? "{CommandType:\"" + BusCommands.Disconnect + "\",Reason:\"" + reason + "\"}"
                        : "{CommandType:\"" + BusCommands.Disconnect + "\"}";
            }
        });
    }
}
