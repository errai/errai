package org.jboss.workspace.server.bus;

import com.google.gwt.user.client.rpc.AsyncCallback;
import org.jboss.workspace.client.framework.AcceptsCallback;

import java.util.List;
import java.util.Set;

public interface MessageBus {
    public void store(String subject, String message);
    public Message nextMessage(Object sessionContext);
    public void subscribe(String subject, AcceptsCallback receiver);
    public void remoteSubscribe(Object sessionContext, String subject);
    public Set<String> getSubjects();
}
