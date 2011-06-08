/*
 * Copyright 2010 JBoss, a divison Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.tools.monitoring;

import org.jboss.errai.bus.client.api.MessageCallback;
import org.jboss.errai.bus.client.api.MessageListener;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.framework.BusMonitor;
import org.jboss.errai.bus.client.framework.MessageBus;

public class ClientBusProxyImpl implements MessageBus {
  private MessageBus serverBus;

  public ClientBusProxyImpl(MessageBus serverBus) {
    this.serverBus = serverBus;
  }

  public void sendGlobal(org.jboss.errai.bus.client.api.Message message) {
  }

  public void send(org.jboss.errai.bus.client.api.Message message) {
  }

  public void send(org.jboss.errai.bus.client.api.Message message, boolean fireListeners) {
  }

  public void conversationWith(org.jboss.errai.bus.client.api.Message message, MessageCallback callback) {
  }

  public void subscribe(String subject, MessageCallback receiver) {
  }

  public void subscribeLocal(String subject, MessageCallback receiver) {
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
