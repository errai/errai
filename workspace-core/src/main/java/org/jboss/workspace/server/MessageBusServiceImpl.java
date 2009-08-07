package org.jboss.workspace.server;

import org.jboss.workspace.client.rpc.MessageBusService;
import org.jboss.workspace.server.bus.MessageBus;
import org.jboss.workspace.server.bus.SimpleMessageBusProvider;
import org.jboss.workspace.server.bus.Message;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;

import javax.servlet.ServletException;
import java.util.Set;

public class MessageBusServiceImpl extends RemoteServiceServlet implements MessageBusService {
    private MessageBus bus;

    @Override
    public void init() throws ServletException {
        // just use the simple bus for now.  more integration options to come...
        bus = new SimpleMessageBusProvider().getBus();
    }

    public void store(String subject, String message) {
        bus.store(subject, message);
    }

    public String[] nextMessage() {
        Message m = bus.nextMessage(getThreadLocalRequest().getSession(true));
        if (m != null) {
            return new String[] { m.getSubject(), m.getMessage() };
        }
        else {
            return null;
        }
    }

    public void remoteSubscribe(String subject) {
        bus.remoteSubscribe(getThreadLocalRequest().getSession(true), subject);
    }

    public Set<String> getSubjects() {
        return bus.getSubjects();
    }
}
