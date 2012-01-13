/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.bus.client;

import org.jboss.errai.bus.client.api.*;
import org.jboss.errai.bus.client.framework.BusMonitor;
import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.client.framework.RequestDispatcher;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * The main GWT <tt>EntryPoint</tt> class for ErraiBus.  This class also contains a static global reference to the
 * client {@link org.jboss.errai.bus.client.framework.MessageBus} which can be obtained by calling: <tt>ErraiBus.get()</tt>
 */
public class ErraiBus implements EntryPoint {
  private static MessageBus bus;

  static {
    if (GWT.isClient()) {
      bus = GWT.create(MessageBus.class);
    }
    else {

      // edge case: in simulated client mode, client code runs in an environment where GWT.isClient() returns false
      // obviously, this code will not be able to contact the server, but framework code still assumes bus != null
      bus = new MessageBus() {
        @Override
        public void sendGlobal(Message message) {

        }

        @Override
        public void send(Message message) {
        }

        @Override
        public void send(Message message, boolean fireListeners) {
        }

        @Override
        public void conversationWith(Message message, MessageCallback callback) {
        }

        @Override
        public void subscribe(String subject, MessageCallback receiver) {
        }

        @Override
        public void subscribeLocal(String subject, MessageCallback receiver) {
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
      };
    }

  }

  /**
   * Obtain an instance of the client MessageBus.
   *
   * @return Returns instance of MessageBus
   */
  public static MessageBus get() {
    return bus;
  }

  private static RequestDispatcher DISPATCHER_INST = new RequestDispatcher() {
    @Override
    public void dispatchGlobal(Message message) {
      get().sendGlobal(message);
    }

    @Override
    public void dispatch(Message message) {
      get().send(message);
    }
  };

  public static RequestDispatcher getDispatcher() {
    return DISPATCHER_INST;
  }

  @Override
  public void onModuleLoad() {
  }
}
