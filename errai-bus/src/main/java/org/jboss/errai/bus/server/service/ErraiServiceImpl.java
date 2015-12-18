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

import static org.slf4j.LoggerFactory.getLogger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.errai.bus.client.api.builder.DefaultRemoteCallBuilder;
import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.client.util.ErrorHelper;
import org.jboss.errai.bus.server.DefaultTaskManager;
import org.jboss.errai.bus.server.QueueUnavailableException;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.io.websockets.WebSocketServer;
import org.jboss.errai.bus.server.service.bootstrap.BootstrapContext;
import org.jboss.errai.bus.server.service.bootstrap.OrderedBootstrap;
import org.slf4j.Logger;

import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * Default implementation of the ErraiBus server-side service.
 */
@Singleton
public class ErraiServiceImpl<S> implements ErraiService<S> {

  private ServerMessageBus bus;
  private ErraiServiceConfigurator config;
  private SessionProvider<S> sessionProvider;
  private RequestDispatcher dispatcher;
  private List<Runnable> shutdownHooks = new ArrayList<Runnable>();
  private final Logger log = getLogger(getClass());

  // Silence WELD-001529
  private ErraiServiceImpl() {}
  
  /**
   * Initializes the errai service with a bus and configurator
   *
   * @param configurator - the configurator to take care of the configuration for the service
   */
  @Inject
  public ErraiServiceImpl(final ErraiServiceConfigurator configurator) {
    this.bus = new ServerMessageBusImpl(this, configurator);
    this.config = configurator;
    boostrap();
  }

  private void boostrap() {
    BootstrapContext context = new BootstrapContext(this, bus, config);
    new OrderedBootstrap().execute(context);

    if (ErraiConfigAttribs.ENABLE_WEB_SOCKET_SERVER.getBoolean(config)) {
      WebSocketServer server = new WebSocketServer(this);
      server.start();
    }
  }

  /**
   * Passes off the message to the bus for handling
   *
   * @param message - the message to store/deliver
   */
  @Override
  public void store(Message message) {
    if (message == null) {
      return;
    }

    message.addResources(config.getResourceProviders());

    /**
     * Pass the message off to the messaging bus for handling.
     */
    try {
      getDispatcher().dispatchGlobal(message);
    }
    catch (QueueUnavailableException e) {
      throw e;
    }
    catch (Throwable t) {
      t.printStackTrace();
      if (!message.hasResource("Exception")) {
        message.setResource("Exception", t.getCause());
        ErrorHelper.sendClientError(bus, message, t.getMessage(), t);
      }
    }
  }

  @Override
  public void store(Collection<Message> messages) {
    for (Message m : messages) {
      store(m);
    }
  }

  @Override
  public void stopService() {
    bus.stop();
    DefaultTaskManager.get().requestStop();

    for (Runnable runnable : shutdownHooks) {
      try {
        runnable.run();
      }
      catch (Throwable e) {
        log.error("error executing shutdown hook", e);
      }
    }
    
    bus = null;
    config = null;
    sessionProvider = null;
    dispatcher = null;
    shutdownHooks = null;

    DefaultRemoteCallBuilder.destroyProxyFactory();
  }

  /**
   * Gets the bus associated with this service
   *
   * @return the bus associated with this service
   */
  @Override
  public ServerMessageBus getBus() {
    return bus;
  }

  /**
   * Gets the configuration used to initalize the service
   *
   * @return the errai service configurator
   */
  @Override
  public ErraiServiceConfigurator getConfiguration() {
    return config;
  }

  @Override
  public void addShutdownHook(Runnable runnable) {
    shutdownHooks.add(runnable);
  }

  @Override
  public SessionProvider<S> getSessionProvider() {
    return sessionProvider;
  }

  @Override
  public void setSessionProvider(SessionProvider<S> sessionProvider) {
    if (this.sessionProvider != null) {
      throw new IllegalStateException("cannot set session provider more than once.");
    }
    this.sessionProvider = sessionProvider;
  }

  @Override
  public RequestDispatcher getDispatcher() {
    return dispatcher;
  }

  @Override
  public void setDispatcher(RequestDispatcher dispatcher) {
    if (this.sessionProvider != null) {
      throw new IllegalStateException("cannot set dispatcher more than once.");
    }
    this.dispatcher = dispatcher;
  }
}
