package org.jboss.workspace.server;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.rpc.MessageBusService;
import org.jboss.workspace.server.bus.Message;
import org.jboss.workspace.server.bus.MessageBus;
import org.jboss.workspace.server.bus.SimpleMessageBusProvider;
import org.jboss.workspace.server.json.JSONUtil;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

public class MessageBusServiceImpl extends RemoteServiceServlet implements MessageBusService {
    private MessageBus bus;

    @Override
    public void init() throws ServletException {
        // just use the simple bus for now.  more integration options to come...
        bus = new SimpleMessageBusProvider().getBus();

        bus.subscribe("ServerEchoService", new AcceptsCallback() {
            public void callback(Object message, Object data) {

                if (message == null) return;
                Map map = JSONUtil.decodeToMap(String.valueOf(message));

                if (map.containsKey("EchoBackData")) {
                    System.out.println("EchoBack: " + map.get("EchoBackData"));
                }
            }
        });

        Thread t = new Thread() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000 * 5);

                    Map<String, Object> msg = new HashMap<String, Object>();
                    msg.put("CommandType", "Hello");
                    msg.put("Name", "Mr. Server");

                    bus.store("org.jboss.workspace.WorkspaceLayout", msg);

                }
                catch (InterruptedException e) {
                }
                
            }
        };

        t.start();


    }

    public void store(String subject, String message) {
        bus.store(subject, message);
    }

    public String[] nextMessage() {
        Message m = bus.nextMessage(getId());
        if (m != null) {
            return new String[]{m.getSubject(), String.valueOf(m.getMessage())};
        }
        else {
            return null;
        }
    }

    public void remoteSubscribe(String subject) {
        if (bus.getSubjects().contains(subject)) return;
        bus.remoteSubscribe(getId(), subject);
    }

    private String getId() {
        HttpServletRequest request = getThreadLocalRequest();
        HttpSession session = request.getSession();

        if (session.getAttribute("WSSessionID") == null) {
            session.setAttribute("WSSessionID", session.getId());
        }

        return (String) session.getAttribute("WSSessionID");
    }

    public String[] getSubjects() {
        return bus.getSubjects().toArray(new String[bus.getSubjects().size()]);
    }
}
