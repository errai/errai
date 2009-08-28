package org.jboss.workspace.server.bus;

import org.jboss.workspace.client.framework.AcceptsCallback;
import org.jboss.workspace.client.framework.MessageCallback;
import org.jboss.workspace.client.rpc.CommandMessage;

import java.util.Map;
import java.util.Set;

public interface MessageBus {
   // public void storeGlobal(String subject, Object message);
    public void storeGlobal(String subject, CommandMessage message);
    public void storeGlobal(String subject, CommandMessage message, boolean fireListeners);

  //  public void store(String sessionId, String subject, Object message);
    public void store(String sessionid, String subject, CommandMessage message);
    public void store(String sessionid, String subject, CommandMessage message, boolean fireListeners);

    public void store(String subject, CommandMessage message);
    public void store(String subject, CommandMessage message, boolean fireListeners);

    public Message nextMessage(Object sessionContext);

    public void subscribe(String subject, MessageCallback receiver);
    public void remoteSubscribe(Object sessionContext, String subject);

    public Set<String> getSubjects();

    public void addGlobalListener(MessageListener listener);
}
