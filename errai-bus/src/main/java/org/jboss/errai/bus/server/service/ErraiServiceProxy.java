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

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.api.SessionProvider;

import java.util.Collection;

/**
 * @author Mike Brock
 */
class ErraiServiceProxy implements ErraiService<Object> {

  private MessageBusProxy messageBusProxy = new MessageBusProxy();
  private RequestDispatcherProxy requestDispatcherProxy = new RequestDispatcherProxy();
  private ErraiService service;

  @Override
  public void store(Message message) {
    service.store(message);
  }

  @Override
  public void store(Collection messages) {
    service.store(messages);
  }

  @Override
  public ServerMessageBus getBus() {
    return messageBusProxy;
  }

  @Override
  public ErraiServiceConfigurator getConfiguration() {
    return service.getConfiguration();
  }

  @Override
  public void addShutdownHook(Runnable runnable) {
    service.addShutdownHook(runnable);
  }

  @Override
  public void stopService() {
    service.stopService();
  }

  @Override
  public SessionProvider getSessionProvider() {
    return service.getSessionProvider();
  }

  @Override
  public void setSessionProvider(SessionProvider sessionProvider) {
    throw new IllegalStateException("cannot set session provider in proxy");

  }

  @Override
  public RequestDispatcher getDispatcher() {
    return requestDispatcherProxy;
  }

  @Override
  public void setDispatcher(RequestDispatcher dispatcher) {
    throw new IllegalStateException("cannot set dispatcher in proxy");
  }

  public void closeProxy(ErraiService service) {
    this.service = service;
    messageBusProxy.closeProxy(service.getBus());
    requestDispatcherProxy.closeProxy(service.getDispatcher());
  }
}
