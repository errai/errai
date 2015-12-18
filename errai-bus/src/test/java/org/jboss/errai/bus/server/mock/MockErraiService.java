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

package org.jboss.errai.bus.server.mock;

import org.jboss.errai.bus.client.api.messaging.Message;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.api.SessionProvider;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public class MockErraiService<S> implements ErraiService<S> {
  @Override
  public void store(Message message) {
  }

  @Override
  public void store(Collection<Message> messages) {
  }

  @Override
  public ServerMessageBus getBus() {
    return null;
  }

  @Override
  public ErraiServiceConfigurator getConfiguration() {
    return null;
  }

  @Override
  public void addShutdownHook(Runnable runnable) {
  }

  @Override
  public void stopService() {
  }

  @Override
  public SessionProvider getSessionProvider() {
    return null;
  }

  @Override
  public void setSessionProvider(SessionProvider sessionProvider) {
  }

  @Override
  public RequestDispatcher getDispatcher() {
    return null;
  }

  @Override
  public void setDispatcher(RequestDispatcher dispatcher) {
  }


}
