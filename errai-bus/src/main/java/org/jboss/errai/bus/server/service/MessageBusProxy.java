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

package org.jboss.errai.bus.server.service;

import com.google.common.collect.LinkedHashMultimap;
import com.google.common.collect.Multimap;
import org.jboss.errai.bus.client.api.BusMonitor;
import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.server.api.MessageQueue;
import org.jboss.errai.bus.server.api.QueueClosedListener;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.common.client.api.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

/**
 * @author Mike Brock
 */
public class MessageBusProxy implements ServerMessageBus {

  // BEGIN: Only referenced in synchronized methods.
  private List<Message> heldGlobalMessages = new ArrayList<Message>();
  private List<Message> heldMessages = new ArrayList<Message>();
  private Map<Message, Boolean> heldMessageFireListener = new LinkedHashMap<Message, Boolean>();
  private Multimap<String, MessageCallback> heldSubscribe = LinkedHashMultimap.create();
  private Multimap<String, MessageCallback> heldLocalSubscribe = LinkedHashMultimap.create();
  private List<SubscribeListener> heldSubscribeListener = new ArrayList<SubscribeListener>();
  private List<UnsubscribeListener> heldUnsubscribeListener = new ArrayList<UnsubscribeListener>();
  private BusMonitor heldBusMonitor;
  // END: Only referenced in synchronized methods.

  /*
   * These fields must be volatile because they are read from unsynchronized methods.
   */
  private volatile ServerMessageBus proxied;

  @Override
  public void sendGlobal(Message message) {
    Assert.notNull("message cannot be null", message);

    if (proxied != null || !offerSendGlobal(message)) {
      proxied.sendGlobal(message);
    }
  }

  private synchronized boolean offerSendGlobal(Message message) {
    if (proxied != null) {
      return false;
    } else {
      heldGlobalMessages.add(message);
      return true;
    }
  }

  @Override
  public void send(Message message) {
    Assert.notNull("message cannot be null", message);

    if (proxied != null || !offerSend(message)) {
      proxied.send(message);
    }
  }

  private synchronized boolean offerSend(Message message) {
    if (proxied != null) {
      return false;
    } else {
      heldMessages.add(message);
      return true;
    }
  }

  @Override
  public void send(Message message, boolean fireListeners) {
    Assert.notNull("message cannot be null", message);

    if (proxied != null || !offerSend(message, fireListeners)) {
      proxied.send(message, fireListeners);
    }
  }

  private synchronized boolean offerSend(Message message, boolean fireListeners) {
    if (proxied != null) {
      return false;
    } else {
      heldMessageFireListener.put(message, fireListeners);
      return true;
    }
  }

  @Override
  public Subscription subscribe(String subject, MessageCallback receiver) {
    Assert.notNull("message callback cannot be null", receiver);

    Subscription subscription = null;
    if (proxied == null) {
      subscription = offerSubscribe(subject, receiver);
    }
    if (subscription == null) {
      subscription = proxied.subscribe(subject, receiver);
    }

    return subscription;
  }

  private synchronized Subscription offerSubscribe(String subject, MessageCallback receiver) {
    if (proxied != null) {
      return null;
    } else {
      heldSubscribe.put(subject, receiver);
      return new Subscription() {
        @Override
        public void remove() {
          throw new IllegalStateException("cannot unsubscribe from a proxied MessageBus");
        }
      };
    }
  }

  @Override
  public Subscription subscribeLocal(String subject, MessageCallback receiver) {
    Assert.notNull("message callback cannot be null", receiver);

    Subscription subscription = null;
    if (proxied == null) {
      subscription = offerSubscribeLocal(subject, receiver);
    }
    if (subscription == null) {
      subscription = proxied.subscribeLocal(subject, receiver);
    }

    return subscription;
  }

  private synchronized Subscription offerSubscribeLocal(String subject, MessageCallback receiver) {
    if (proxied != null) {
      return null;
    } else {
      heldLocalSubscribe.put(subject, receiver);
      return new Subscription() {
        @Override
        public void remove() {
          throw new IllegalStateException("cannot unsubscribe from a proxied MessageBus");
        }
      };
    }
  }

  @Override
  public void unsubscribeAll(String subject) {
    if (proxied != null) {
      proxied.unsubscribeAll(subject);
    }
  }

  @Override
  public synchronized boolean isSubscribed(String subject) {
    if (proxied != null) {
      return proxied.isSubscribed(subject);
    } else {
      return heldSubscribe.containsKey(subject) || heldLocalSubscribe.containsKey(subject);
    }
  }

  @Override
  public void addSubscribeListener(SubscribeListener listener) {
    Assert.notNull("subscribe listener cannot be null", listener);

    if (proxied != null || !offerAddSubscribeListener(listener)) {
      proxied.addSubscribeListener(listener);
    }
  }

  private synchronized boolean offerAddSubscribeListener(SubscribeListener listener) {
    if (proxied != null) {
      return false;
    } else {
      heldSubscribeListener.add(listener);
      return true;
    }
  }

  @Override
  public void addUnsubscribeListener(UnsubscribeListener listener) {
    Assert.notNull("unsubscribe listener cannot be null", listener);

    if (proxied != null || !offerAddUnsubscribeListener(listener)) {
      proxied.addUnsubscribeListener(listener);
    }
  }

  private synchronized boolean offerAddUnsubscribeListener(UnsubscribeListener listener) {
    if (proxied != null) {
      return false;
    } else {
      heldUnsubscribeListener.add(listener);
      return true;
    }
  }

  @Override
  public MessageQueue getQueue(QueueSession session) {
    return proxied.getQueue(session);
  }

  @Override
  public void closeQueue(String sessionId) {
    proxied.closeQueue(sessionId);
  }

  @Override
  public void closeQueue(MessageQueue queue) {
    proxied.closeQueue(queue);
  }

  @Override
  public ExecutorService getScheduler() {
    return proxied.getScheduler();
  }

  @Override
  public void addQueueClosedListener(QueueClosedListener listener) {
    proxied.addQueueClosedListener(listener);
  }

  @Override
  public void configure(ErraiServiceConfigurator service) {
    proxied.configure(service);
  }

  @Override
  public Collection<MessageCallback> getReceivers(String subject) {
    Collection<MessageCallback> receivers = null;
    if (proxied == null) {
      receivers = offerGetReceivers(subject);
    }
    if (receivers == null) {
      receivers = proxied.getReceivers(subject);
    }

    return receivers;
  }

  private synchronized Collection<MessageCallback> offerGetReceivers(String subject) {
    if (proxied != null) {
      return null;
    } else {
      return new ArrayList<MessageCallback>(heldSubscribe.values());
    }
  }

  @Override
  public boolean hasRemoteSubscriptions(String subject) {
    return proxied.hasRemoteSubscriptions(subject);
  }

  @Override
  public boolean hasRemoteSubscription(String sessionId, String subject) {
    return proxied.hasRemoteSubscription(sessionId, subject);
  }

  @Override
  public Map<QueueSession, MessageQueue> getMessageQueues() {
    return proxied.getMessageQueues();
  }

  @Override
  public MessageQueue getQueueBySession(String id) {
    return proxied.getQueueBySession(id);
  }

  @Override
  public QueueSession getSessionBySessionId(String id) {
    return proxied.getSessionBySessionId(id);
  }

  @Override
  public void associateNewQueue(QueueSession oldSession, QueueSession newSession) {
    proxied.associateNewQueue(oldSession, newSession);
  }

  @Override
  public void stop() {
    proxied.stop();
  }

  @Override
  public Message getDeadLetterMessage(String messageId) {
    return proxied.getDeadLetterMessage(messageId);
  }

  @Override
  public boolean removeDeadLetterMessage(String messageId) {
     return proxied.removeDeadLetterMessage(messageId);
  }

  @Override
  public synchronized void attachMonitor(BusMonitor monitor) {
    this.heldBusMonitor = monitor;
  }

  public synchronized void closeProxy(ServerMessageBus bus) {
    Assert.notNull("message bus reference cannot be null", bus);

    if (heldBusMonitor != null) {
      bus.attachMonitor(heldBusMonitor);
    }

    for (Map.Entry<String, MessageCallback> entry : heldSubscribe.entries()) {
      bus.subscribe(entry.getKey(), entry.getValue());
    }

    for (Map.Entry<String, MessageCallback> entry : heldLocalSubscribe.entries()) {
      bus.subscribeLocal(entry.getKey(), entry.getValue());
    }

    for (SubscribeListener subscribeListener : heldSubscribeListener) {
      bus.addSubscribeListener(subscribeListener);
    }

    for (UnsubscribeListener unsubscribeListener : heldUnsubscribeListener) {
      bus.addUnsubscribeListener(unsubscribeListener);
    }

    for (Message message : heldMessages) {
      bus.send(message);
    }

    for (Message message : heldGlobalMessages) {
      bus.sendGlobal(message);
    }

    for (Map.Entry<Message, Boolean> entry : heldMessageFireListener.entrySet()) {
      bus.send(entry.getKey(), entry.getValue());
    }

    reset();
    this.proxied = bus;
  }

  public void reset() {
    this.proxied = null;
    this.heldBusMonitor = null;
    this.heldSubscribe.clear();
    this.heldLocalSubscribe.clear();
    this.heldSubscribeListener.clear();
    this.heldUnsubscribeListener.clear();
    this.heldMessages.clear();
    this.heldGlobalMessages.clear();
    this.heldMessageFireListener.clear();
  }
}
