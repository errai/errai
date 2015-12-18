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

import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 * Bootstrap Errai in an ordered fashion.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 * @see org.jboss.errai.bus.server.service.bootstrap.BootstrapExecution
 */
public class OrderedBootstrap implements BootstrapExecution {
  private Logger log = LoggerFactory.getLogger(OrderedBootstrap.class);

  private List<BootstrapExecution> bootstrap = new LinkedList<BootstrapExecution>();

  public OrderedBootstrap() {
    bootstrap.add(new DefaultComponents());
    bootstrap.add(new DefaultServices());
    bootstrap.add(new LockDownServices());
    bootstrap.add(new LoadExtensions());
    bootstrap.add(new DefaultResources());
    bootstrap.add(new RegisterTypes());
    bootstrap.add(new DiscoverServices());
    bootstrap.add(new BusConfiguration());
    bootstrap.add(new FinishInit());
  }

  public void execute(final BootstrapContext context) {
    log.info("starting errai bus ...");

    try {
      for (BootstrapExecution execution : bootstrap) {
        execution.execute(context);
      }
      context.executeDeferred();
      ((ErraiServiceConfiguratorImpl) context.getConfig()).lockdown();
      log.info("errai bus started.");
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new RuntimeException("server bootstrap failed", e);
    }
  }
}
