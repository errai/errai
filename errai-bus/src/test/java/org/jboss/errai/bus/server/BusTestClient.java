package org.jboss.errai.bus.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.sun.tools.internal.ws.wsdl.document.MessagePart;
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
import org.jboss.errai.common.client.protocols.MessageParts;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class BusTestClient implements MessageBus {
  private final MessageBus remoteBus;
  private final QueueSession clientSession = MockQueueSessionFactory.newSession();

  private final Multimap<String, MessageCallback> services = HashMultimap.create();
  private final Multimap<String, MessageCallback> localServices = HashMultimap.create();
  private final Set<String> remotes = new HashSet<String>();

  private final List<SubscribeListener> subscribeListeners = new ArrayList<SubscribeListener>();
  private final List<UnsubscribeListener> unsubscribeListeners = new ArrayList<UnsubscribeListener>();

  public BusTestClient(final MessageBus remoteBus) {
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
  public void sendGlobal(final Message message) {
    send(message);
  }

  @Override
  public void send(final Message message) {
    send(message, true);
  }

  @Override
  public void send(final Message message, final boolean fireListeners) {
    for (final MessageCallback messageCallback : services.get(message.getSubject())) {
      messageCallback.callback(message);
    }
  }

  @Override
  public Subscription subscribe(final String subject, final MessageCallback receiver) {
    services.put(subject, receiver);

    return new Subscription() {
      @Override
      public void remove() {
        services.remove(subject, receiver);
      }
    };
  }

  @Override
  public Subscription subscribeLocal(final String subject, final MessageCallback receiver) {
    localServices.put(subject, receiver);

    return new Subscription() {
      @Override
      public void remove() {
        localServices.remove(subject, receiver);
      }
    };
  }

  @Override
  public void unsubscribeAll(final String subject) {
    services.removeAll(subject);
  }

  @Override
  public boolean isSubscribed(final String subject) {
    return services.containsKey(subject);
  }

  @Override
  public void addGlobalListener(final MessageListener listener) {
  }

  @Override
  public void addSubscribeListener(final SubscribeListener listener) {
    subscribeListeners.add(listener);
  }

  @Override
  public void addUnsubscribeListener(final UnsubscribeListener listener) {
    unsubscribeListeners.add(listener);
  }

  @Override
  public void attachMonitor(final BusMonitor monitor) {
  }


  private class BusControlProtocolCallback implements MessageCallback {
    @Override
    @SuppressWarnings({"unchecked"})
    public void callback(final Message message) {
      switch (BusCommands.valueOf(message.getCommandType())) {
        case RemoteSubscribe:
          if (message.hasPart(MessageParts.SubjectsList)) {
            for (final String subject : (List<String>) message.get(List.class, MessageParts.SubjectsList)) {
              remotes.add(subject);
            }
          }
          else if (message.hasPart(MessageParts.Subject)) {
            remotes.add(message.get(String.class, MessageParts.Subject));
          }
          break;

        case RemoteUnsubscribe:
          unsubscribeAll(message.get(String.class, MessageParts.Subject));
          break;

        case CapabilitiesNotice:
          for (final String capability : message.get(String.class, MessageParts.CapabilitiesFlags).split(",")) {

          }

      }

    }
  }


}
