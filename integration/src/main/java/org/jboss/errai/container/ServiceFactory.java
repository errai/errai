/*
 * Copyright 2009 JBoss, a divison Red Hat, Inc
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
package org.jboss.errai.container;

import org.jboss.errai.bus.client.framework.MessageBus;
import org.jboss.errai.bus.server.ServerMessageBusImpl;
import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.service.ErraiServiceImpl;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;

/**
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: Sep 29, 2010
 */
public class ServiceFactory {
  public static ErraiService create() {
    return Guice.createInjector(new AbstractModule() {
      public void configure() {
        bind(MessageBus.class).to(ServerMessageBusImpl.class);
        bind(ServerMessageBus.class).to(ServerMessageBusImpl.class);
        bind(ErraiService.class).to(ErraiServiceImpl.class);
        bind(ErraiServiceConfigurator.class).to(ErraiServiceConfiguratorImpl.class);
      }
    }).getInstance(ErraiService.class);

  }
}
