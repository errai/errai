/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.tools.monitoring;

import org.jboss.errai.bus.client.api.messaging.MessageCallback;
import org.jboss.errai.bus.client.api.messaging.MessageListener;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.BusMonitor;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.messaging.Message;

public class ClientBusProxyImpl implements MessageBus {
  private MessageBus serverBus;

  public ClientBusProxyImpl(MessageBus serverBus) {
    this.serverBus = serverBus;
  }

  public void sendGlobal(Message message) {
  }

  public void send(Message message) {
  }

  public void send(Message message, boolean fireListeners) {
  }

  public void conversationWith(Message message, MessageCallback callback) {
  }

  public Subscription subscribe(String subject, MessageCallback receiver) {
    return null;
  }

  public Subscription subscribeLocal(String subject, MessageCallback receiver) {
    return null;
  }

  public void unsubscribeAll(String subject) {
  }

  public boolean isSubscribed(String subject) {
    return false;
  }

  public void addGlobalListener(MessageListener listener) {
  }

  public void addSubscribeListener(SubscribeListener listener) {
  }

  public void addUnsubscribeListener(UnsubscribeListener listener) {
  }

  public void attachMonitor(BusMonitor monitor) {
  }
}
