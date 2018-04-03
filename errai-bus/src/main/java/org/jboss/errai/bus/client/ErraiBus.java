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

package org.jboss.errai.bus.client;

import java.util.Collections;
import java.util.Set;

import org.jboss.errai.bus.client.api.BusLifecycleListener;
import org.jboss.errai.bus.client.api.BusMonitor;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.bus.client.api.SubscribeListener;
import org.jboss.errai.bus.client.api.Subscription;
import org.jboss.errai.bus.client.api.TransportErrorHandler;
import org.jboss.errai.bus.client.api.UnsubscribeListener;
import org.jboss.errai.bus.client.api.messaging.*;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

/**
 * The main GWT <tt>EntryPoint</tt> class for ErraiBus.  This class also contains a static global reference to the
 * client {@link org.jboss.errai.bus.client.api.messaging.MessageBus} which can be obtained by calling: <tt>ErraiBus.get()</tt>
 */
public class ErraiBus implements EntryPoint {
  private static ClientMessageBus bus;
  private static Logger logger = LoggerFactory.getLogger(ErraiBus.class);

  static {
    if (GWT.isClient()) {
      logger.debug("Creating client message bus");
      bus = GWT.create(MessageBus.class);
    }
    else {

      logger.debug("Creating simulated client message bus");
      // edge case: in simulated client mode, client code runs in an environment where GWT.isClient() returns false
      // obviously, this code will not be able to contact the server, but framework code still assumes bus != null
      bus = new ClientMessageBus() {
        @Override
        public void sendGlobal(final Message message) {

        }

        @Override
        public void send(final Message message) {
        }

        @Override
        public void send(final Message message, final boolean fireListeners) {
        }

        @Override
        public void sendLocal(final Message message) {
        }

        @Override
        public Subscription subscribe(final String subject, final MessageCallback receiver) {
          return null;
        }

        @Override
        public Subscription subscribeLocal(final String subject, final MessageCallback receiver) {
          return null;
        }

        @Override
        public Subscription subscribeShadow(final String subject, final MessageCallback callback) {
          return null;
        }

        @Override
        public void unsubscribeAll(final String subject) {
        }

        @Override
        public boolean isSubscribed(final String subject) {
          return false;
        }

        @Override
        public void addSubscribeListener(final SubscribeListener listener) {
        }

        @Override
        public void addUnsubscribeListener(final UnsubscribeListener listener) {
        }

        @Override
        public void addInterceptor(MessageInterceptor interceptor) {
        }

        @Override
        public void removeInterceptor(MessageInterceptor interceptor) {
        }

        @Override
        public void attachMonitor(final BusMonitor monitor) {
        }

        @Override
        public void init() {
        }

        @Override
        public void stop(final boolean sendDisconnectToServer) {
        }

        @Override
        public Set<String> getAllRegisteredSubjects() {
          return Collections.emptySet();
        }

        @Override
        public void addTransportErrorHandler(final TransportErrorHandler errorHandler) {
        }

        @Override
        public void removeTransportErrorHandler(final TransportErrorHandler errorHandler) {
        }

        @Override
        public void addLifecycleListener(final BusLifecycleListener l) {
        }

        @Override
        public void removeLifecycleListener(final BusLifecycleListener l) {
        }

        @Override
        public void setProperty(final String name, final String value) {
        }

        @Override
        public void clearProperties() {
        }

        @Override
        public String getSessionId() {
          return null;
        }

        @Override
        public String getClientId() {
          return null;
        }
      };
    }

    InitVotes.registerPersistentPreInitCallback(new Runnable() {
      @Override
      public void run() {
        logger.debug("Initializing client message bus.");
        bus.init();
      }
    });
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
    public void dispatchGlobal(final Message message) {
      get().sendGlobal(message);
    }

    @Override
    public void dispatch(final Message message) {
      get().send(message);
    }
  };

  public static RequestDispatcher getDispatcher() {
    return DISPATCHER_INST;
  }

  @Override
  public void onModuleLoad() {
  }

  String getSessionId() {
    return null;
  }

  String getClientId() {
    return null;
  }
}
