package org.jboss.errai.bus.server;

import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.MessageListener;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.framework.BusMonitor;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.Subscription;
import org.jboss.errai.bus.client.protocols.BusCommands;

import java.util.HashMap;

/**
 * @author Mike Brock
 */
public class BusTestClient implements MessageBus {
  private final MessageBus remoteBus;
  private final QueueSession clientSession = MockQueueSessionFactory.newSession();

  public BusTestClient(MessageBus remoteBus) {
    this.remoteBus = remoteBus;
  }

  public void connect() {
    final Message m = CommandMessage.createWithParts(new HashMap<String, Object>())
        .toSubject("ServerBus")
        .command(BusCommands.ConnectToQueue)
        .setResource("Session", clientSession);

    remoteBus.sendGlobal(m);
  }


  @Override
  public void sendGlobal(Message message) {
    send(message);
  }

  @Override
  public void send(Message message) {
  }

  @Override
  public void send(Message message, boolean fireListeners) {
  }

  @Override
  public Subscription subscribe(String subject, MessageCallback receiver) {
    return null;
  }

  @Override
  public Subscription subscribeLocal(String subject, MessageCallback receiver) {
    return null;
  }

  @Override
  public void unsubscribeAll(String subject) {
  }

  @Override
  public boolean isSubscribed(String subject) {
    return false;
  }

  @Override
  public void addGlobalListener(MessageListener listener) {
  }

  @Override
  public void addSubscribeListener(SubscribeListener listener) {
  }

  @Override
  public void addUnsubscribeListener(UnsubscribeListener listener) {
  }

  @Override
  public void attachMonitor(BusMonitor monitor) {
  }
}
