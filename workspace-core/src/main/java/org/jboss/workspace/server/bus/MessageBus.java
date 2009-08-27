package org.jboss.workspace.server.bus;

import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.MessageCallback;

import java.util.Map;
import java.util.Set;

public interface MessageBus {
    public void store(String subject, Object message);
    public void store(String subject, Map<String, Object> message);

    public Message nextMessage(Object sessionContext);

    public void subscribe(String subject, MessageCallback receiver);
    public void remoteSubscribe(Object sessionContext, String subject);

    public Set<String> getSubjects();

    public void addGlobalListener(MessageListener listener);
}
