package org.jboss.workspace.server.bus;

import org.jboss.workspace.client.framework.AcceptsCallback;

import java.util.Map;
import java.util.Set;

public interface MessageBus {
    public void store(String subject, Object message);
    public void store(String subject, Map<String, Object> message);

    public Message nextMessage(Object sessionContext);

    public void subscribe(String subject, AcceptsCallback receiver);
    public void remoteSubscribe(Object sessionContext, String subject);

    public Set<String> getSubjects();
}
