/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.bus.server;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.bus.client.api.BusMonitor;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.RoutingFlag;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.base.CommandMessage;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.client.protocols.BusCommand;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.io.MessageFactory;
import org.jboss.errai.bus.server.io.OutputStreamWriteAdapter;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.common.client.api.ResourceProvider;
import org.jboss.errai.common.client.protocols.MessageParts;
import org.jboss.errai.common.client.protocols.Resources;
import org.jboss.errai.marshalling.server.util.UnwrappedByteArrayOutputStream;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
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
  private ServerMessageBus remoteBus;
  private final QueueSession serverSession = MockQueueSessionFactory.newSession();
  private final QueueSession localSession = MockQueueSessionFactory.newSession();
  private final RequestDispatcher requestDispatcher = new TestRequestDispatcher();

  private final Multimap<String, MessageCallback> services = HashMultimap.create();
  private final Multimap<String, MessageCallback> localServices = HashMultimap.create();
  private final Set<String> remotes = new HashSet<String>();

  private final List<SubscribeListener> subscribeListeners = new ArrayList<SubscribeListener>();
  private final List<UnsubscribeListener> unsubscribeListeners = new ArrayList<UnsubscribeListener>();

  private final List<Message> deferredDeliveryList = new ArrayList<Message>();

  private final List<Runnable> initCallbacks = new ArrayList<Runnable>();

  private boolean init = false;

  private final ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);

  private BusTestClient(final ServerMessageBus remoteBus) {
    this.remoteBus = remoteBus;

    subscribe("ClientBus", new BusControlProtocolCallback());
  }

  public static BusTestClient create(final ErraiService service) {
    return new BusTestClient(service.getBus());
  }

  private class PollingRunnable implements Runnable {
    @Override
    public void run() {
      final UnwrappedByteArrayOutputStream outputStream = new UnwrappedByteArrayOutputStream();
      final OutputStreamWriteAdapter writeAdapter = new OutputStreamWriteAdapter(outputStream);

      try {
        if (!remoteBus.getMessageQueues().get(serverSession).poll(writeAdapter)) {
          return;
        }
      }
      catch (IOException e) {
        throw new RuntimeException("failed to poll remote bus", e);
      }

      try {
        final List<Message> messages = MessageFactory.createCommandMessage(localSession,
            new ByteArrayInputStream(outputStream.toByteArray()));

        for (final Message message : messages) {
          send(message);
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
    remoteBus.sendGlobal(CommandMessage.create()
        .toSubject("ServerBus")
        .command(BusCommand.Associate)
        .set(MessageParts.RemoteServices, getAdvertisableSubjects())
        .set(MessageParts.PriorityProcessing, "1")
        .setResource("Session", serverSession)
        .setFlag(RoutingFlag.FromRemote));

    executorService.scheduleAtFixedRate(new PollingRunnable(), 0, 10, TimeUnit.MILLISECONDS);
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

    final Message message = CommandMessage.create()
        .toSubject("ServerBus")
        .command(BusCommand.RemoteSubscribe)
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

    final Message message = CommandMessage.create()
        .toSubject("ServerBus")
        .command(BusCommand.RemoteUnsubscribe)
        .set(MessageParts.Subject, subject);

    sendToRemote(message);
  }

  @Override
  public boolean isSubscribed(final String subject) {
    return services.containsKey(subject);
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

  private void sendToRemote(final Message message) {
    message.setResource(Resources.Session.name(), serverSession);
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
      final List<Message> deferred = new ArrayList<Message>();
      for (final Message message : deferredDeliveryList) {
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

      for (final Message message : deferred) {
        remoteBus.sendGlobal(message);
      }
    }
  }

  private class BusControlProtocolCallback implements MessageCallback {
    @Override
    @SuppressWarnings({"unchecked"})
    public void callback(final Message message) {
      switch (BusCommand.valueOf(message.getCommandType())) {
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

        case FinishAssociation:
          Collections.addAll(remotes, message.get(String.class, MessageParts.RemoteServices).split(","));

          init = true;
          drainDeliveryList();

          scheduleInit();
          break;
      }
    }
  }

  private void fireInitCallbacks() {
    for (final Runnable init : initCallbacks) {
      init.run();
    }
  }

  private boolean initScheduled = false;
  private boolean finishedInit = false;

  private void scheduleInit() {
    if (!initScheduled) {
      initScheduled = true;
      executorService.schedule(new Runnable() {
        @Override
        public void run() {
          fireInitCallbacks();
          finishedInit = true;
        }
      }, 250, TimeUnit.MILLISECONDS);
    }
  }

  public void addInitCallback(final Runnable runnable) {
    if (finishedInit) {
      runnable.run();
    }
    else if (initScheduled) {
      throw new RuntimeException("added callback when init is already scheduled!");
    }
    else {
      initCallbacks.add(runnable);
    }
  }

  public void clearInitCallbacks() {
    initCallbacks.clear();
  }

  private String getAdvertisableSubjects() {
    final StringBuilder stringBuilder = new StringBuilder();
    for (final String s : services.keySet()) {
      if (s.startsWith("local:"))
        continue;
      if (!remotes.contains(s)) {
        if (stringBuilder.length() != 0) {
          stringBuilder.append(",");
        }
        stringBuilder.append(s);
      }
    }
    return stringBuilder.toString();
  }

  private class TestRequestDispatcher implements RequestDispatcher {
    @Override
    public void dispatchGlobal(final Message message) throws Exception {
      remoteBus.sendGlobal(message);
    }

    @Override
    public void dispatch(final Message message) throws Exception {
      remoteBus.send(message);
    }
  }

  public void changeBus(final ErraiService service) {
    remoteBus.closeQueue(serverSession.getSessionId());
    remoteBus = service.getBus();

    init = false;
    initScheduled = false;
    finishedInit = false;

    connect();
  }

  public QueueSession getServerSession() {
    return serverSession;
  }
}
