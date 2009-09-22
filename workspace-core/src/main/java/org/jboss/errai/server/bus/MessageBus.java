package org.jboss.errai.server.bus;

import org.jboss.errai.client.framework.MessageCallback;
import org.jboss.errai.client.rpc.CommandMessage;

import java.util.Set;

public interface MessageBus {
    public void storeGlobal(String subject, CommandMessage message);
    public void storeGlobal(String subject, CommandMessage message, boolean fireListeners);

    public void store(String sessionid, String subject, CommandMessage message);
    public void store(String sessionid, String subject, CommandMessage message, boolean fireListeners);

    public void store(String subject, CommandMessage message);
    public void store(String subject, CommandMessage message, boolean fireListeners);

    public Message nextMessage(Object sessionContext);

    public void subscribe(String subject, MessageCallback receiver);

    public void remoteSubscribe(Object sessionContext, String subject);
    public void remoteUnsubscribe(Object sessionContext, String subject);

    public Set<String> getSubjects();

    public void addGlobalListener(MessageListener listener);
    public void addSubscribeListener(SubscribeListener listener);
    public void addUnsubscribeListener(UnsubscribeListener listener);
}
