package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.CommandMessage;
import org.jboss.errai.bus.client.MessageCallback;
import org.jboss.errai.bus.client.MessageListener;
import org.jboss.errai.bus.client.Payload;
import org.jboss.errai.bus.client.SubscribeListener;
import org.jboss.errai.bus.client.UnsubscribeListener;


public interface MessageBus {
    public static final String WS_SESSION_ID = "WSSessionID";

    public void sendGlobal(CommandMessage message);

    public void send(CommandMessage message);
    public void send(String subject, CommandMessage message);

    public void send(CommandMessage message, boolean fireListeners);
    public void send(String subject, CommandMessage message, boolean fireListener);

    public void conversationWith(CommandMessage message, MessageCallback callback);

    public Payload nextMessage(Object sessionContext);

    public void subscribe(String subject, MessageCallback receiver);
    public void unsubscribeAll(String subject);

    public boolean isSubscribed(String subject);

    public void addGlobalListener(MessageListener listener);
    public void addSubscribeListener(SubscribeListener listener);
    public void addUnsubscribeListener(UnsubscribeListener listener);
}
