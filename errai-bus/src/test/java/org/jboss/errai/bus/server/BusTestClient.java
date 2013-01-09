package org.jboss.errai.bus.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.bus.client.api.Message;
import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.MessageListener;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.base.MessageBuilder;
import org.jboss.errai.bus.client.framework.BusMonitor;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;
import org.jboss.errai.bus.client.framework.RoutingFlag;
import org.jboss.errai.bus.client.framework.Subscription;
import org.jboss.errai.bus.client.protocols.BusCommands;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.io.OutputStreamWriteAdapter;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.marshalling.server.util.UnwrappedByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * This bus implementation is NOT thread-safe and is for testing purposes only.
 *
 * @author Mike Brock
 */
public class BusTestClient implements MessageBus {
  private final ServerMessageBus remoteBus;
  private final QueueSession serverSession = MockQueueSessionFactory.newSession();
  private final QueueSession localSession = MockQueueSessionFactory.newSession();
  private final RequestDispatcher requestDispatcher = new TestRequestDispatcher();

  private final Multimap<String, MessageCallback> services = HashMultimap.create();
  private final Multimap<String, MessageCallback> localServices = HashMultimap.create();
  private final Set<String> remotes = new HashSet<String>();

  private final List<SubscribeListener> subscribeListeners = new ArrayList<SubscribeListener>();
  private final List<UnsubscribeListener> unsubscribeListeners = new ArrayList<UnsubscribeListener>();

  private final List<Message> deferredDeliveryList = new ArrayList<Message>();

  private boolean init = false;

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

  public BusTestClient(final ServerMessageBus remoteBus) {
    this.remoteBus = remoteBus;

    subscribe("ClientBus", new BusControlProtocolCallback());
  }

  private class PollingRunnable implements Runnable {
    @Override
    public void run() {

      final UnwrappedByteArrayOutputStream outputStream = new UnwrappedByteArrayOutputStream();
      final OutputStreamWriteAdapter writeAdapter = new OutputStreamWriteAdapter(outputStream);

      try {
        if (!remoteBus.getMessageQueues().get(serverSession).poll(false, writeAdapter)) {
          return;
        }
      }
      catch (IOException e) {
        throw new RuntimeException("failed to poll remote bus", e);
      }

      try {
        ByteArrayInputStream inStream = new ByteArrayInputStream(outputStream.toByteArray());

        final List<Message> messages = MessageFactory.createCommandMessage(localSession, inStream);

        for (Message m : messages) {
          send(m);
        }
      }
      catch (UnsupportedEncodingException e) {
        e.printStackTrace();
      }
      catch (IOException e) {
        e.printStackTrace();
      }

    }
  }

  public void connect() {
    final Message m = CommandMessage.createWithParts(new HashMap<String, Object>())
        .toSubject("ServerBus")
        .command(BusCommands.ConnectToQueue)
        .set(MessageParts.PriorityProcessing, "1")
        .setResource("Session", serverSession);

    m.setFlag(RoutingFlag.FromRemote);

    remoteBus.sendGlobal(m);

    executorService.scheduleAtFixedRate(new PollingRunnable(), 100, 100, TimeUnit.MILLISECONDS);
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

    if (init) {
      if (remotes.contains(message.getSubject())) {
        sendToRemote(message);
      }
    }
    else {
      sendToRemote(message);
    }
  }

  @Override
  public Subscription subscribe(final String subject, final MessageCallback receiver) {
    services.put(subject, receiver);

    final Message message = CommandMessage.createWithParts(new HashMap<String, Object>())
        .toSubject("ServerBus")
        .command(BusCommands.RemoteSubscribe)
        .set(MessageParts.PriorityProcessing, "1")
        .set(MessageParts.Subject, subject);

    sendToRemote(message);

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

    final Message message = CommandMessage.createWithParts(new HashMap<String, Object>())
        .toSubject("ServerBus")
        .command(BusCommands.RemoteUnsubscribe)
        .set(MessageParts.Subject, subject);

    sendToRemote(message);
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

  private void sendToRemote(Message message) {
    if (!init) {
      deferredDeliveryList.add(message);
    }
    else {
      remoteBus.sendGlobal(message);
    }
  }

  private void drainDeliveryList() {

    if (init) {
      // only send priority processing messages first.
      List<Message> deferred = new ArrayList<Message>();
      for (Message message : deferredDeliveryList) {
        message.setResource("Session", serverSession);
        message.setFlag(RoutingFlag.FromRemote);
        message.setResource(RequestDispatcher.class.getName(), new ResourceProvider<RequestDispatcher>() {
          @Override
          public RequestDispatcher get() {
            return requestDispatcher;
          }
        });

        if (message.hasPart(MessageParts.PriorityProcessing)) {
          if (remotes.contains(message.getSubject())) {
            remoteBus.sendGlobal(message);
          }
        }
        else {
          deferred.add(message);
        }
      }
      deferredDeliveryList.clear();

      for (Message message : deferred) {
        remoteBus.sendGlobal(message);
      }
    }
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
          break;

        case FinishStateSync:
          final Message remoteSubscribeMessage = CommandMessage.createWithParts(new HashMap<String, Object>())
              .toSubject("ServerBus")
              .command(BusCommands.RemoteSubscribe)
              .set(MessageParts.SubjectsList, new ArrayList<String>(services.keySet()))
              .set(MessageParts.PriorityProcessing, "1")
              .setResource("Session", serverSession);

          remoteSubscribeMessage.setFlag(RoutingFlag.FromRemote);

          remoteBus.sendGlobal(remoteSubscribeMessage);

          final Message stateSyncMessage = CommandMessage.createWithParts(new HashMap<String, Object>())
              .toSubject("ServerBus")
              .command(BusCommands.FinishStateSync)
              .set(MessageParts.PriorityProcessing, "1")
              .setResource("Session", serverSession);

          stateSyncMessage.setFlag(RoutingFlag.FromRemote);

          remoteBus.sendGlobal(stateSyncMessage);

          init = true;
          drainDeliveryList();
          break;
      }
    }
  }

  private class TestRequestDispatcher implements RequestDispatcher {
    @Override
    public void dispatchGlobal(Message message) throws Exception {
      remoteBus.sendGlobal(message);
    }

    @Override
    public void dispatch(Message message) throws Exception {
      remoteBus.send(message);
    }
  }

}
