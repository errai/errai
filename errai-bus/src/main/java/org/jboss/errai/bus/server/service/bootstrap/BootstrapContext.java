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

package org.jboss.errai.bus.server.service.bootstrap;

import org.jboss.errai.bus.server.api.ServerMessageBus;
import org.jboss.errai.bus.server.service.ErraiService;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.common.metadata.MetaDataScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Stack;

/**
 * Provides a shared context to the bootstrap execution.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 */
public class BootstrapContext {
  private final ServerMessageBus bus;
  private final ErraiServiceConfigurator config;
  private final ErraiService service;

  private Stack<Runnable> deferredTasks = new Stack<Runnable>();

  private Logger log = LoggerFactory.getLogger(BootstrapContext.class);

  public BootstrapContext(final ErraiService service,
                          final ServerMessageBus bus,
                          final ErraiServiceConfigurator config) {
    this.service = service;
    this.bus = bus;
    this.config = config;
  }

  public ServerMessageBus getBus() {
    return bus;
  }

  public ErraiServiceConfigurator getConfig() {
    return config;
  }

  public MetaDataScanner getScanner() {
    return config.getMetaDataScanner();
  }

  public void defer(final Runnable task) {
    this.deferredTasks.push(task);
  }

  public ErraiService getService() {
    return service;
  }

  void executeDeferred() {
    log.debug("running deferred bootstrap tasks ...");

    while (!deferredTasks.isEmpty()) {
      final Runnable task = deferredTasks.pop();
      task.run();
    }
  }
}
