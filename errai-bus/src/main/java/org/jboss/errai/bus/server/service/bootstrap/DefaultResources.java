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

import org.jboss.errai.bus.client.api.QueueSession;
import org.jboss.errai.common.client.api.tasks.TaskManager;
import org.jboss.errai.common.client.api.tasks.TaskManagerFactory;
import org.jboss.errai.bus.client.api.laundry.LaundryList;
import org.jboss.errai.bus.client.api.laundry.LaundryListProvider;
import org.jboss.errai.bus.client.api.laundry.LaundryListProviderFactory;
import org.jboss.errai.bus.client.api.messaging.MessageBus;
import org.jboss.errai.bus.client.api.messaging.RequestDispatcher;
import org.jboss.errai.common.client.api.tasks.TaskManagerProvider;
import org.jboss.errai.bus.server.DefaultTaskManager;
import org.jboss.errai.bus.server.service.ErraiServiceConfigurator;
import org.jboss.errai.bus.server.service.ErraiServiceConfiguratorImpl;
import org.jboss.errai.bus.server.util.ServerLaundryList;

/**
 * Setup the default resource providers.
 *
 * @author: Heiko Braun <hbraun@redhat.com>
 * @date: May 3, 2010
 * @see org.jboss.errai.common.client.api.ResourceProvider
 */
class DefaultResources implements BootstrapExecution {
  public void execute(final BootstrapContext context) {
    final ErraiServiceConfiguratorImpl config = (ErraiServiceConfiguratorImpl) context
        .getConfig();

    config.getResourceProviders().put(MessageBus.class.getName(),
        new BusProvider(context.getBus()));
    config.getResourceProviders().put(RequestDispatcher.class.getName(),
        new DispatcherProvider(context.getService().getDispatcher()));

    // configure the server-side taskmanager

    final TaskManager taskManager = resolveTaskManager(config);

    TaskManagerFactory.setTaskManagerProvider(new TaskManagerProvider() {
      public TaskManager get() {
        return taskManager;
      }
    });

    LaundryListProviderFactory
        .setLaundryListProvider(new LaundryListProvider() {
          public LaundryList getLaundryList(final Object ref) {
            return ServerLaundryList.get((QueueSession) ref);
          }
        });
  }

  private TaskManager resolveTaskManager(final ErraiServiceConfigurator config) {
    TaskManager result;
    final String tmProp = config.getProperty("errai.taskmanager_implementation");
    if (tmProp != null) {
      try {
        final Class<?> tm = DefaultResources.class.getClassLoader().loadClass(
            tmProp);
        result = (TaskManager) tm.newInstance();
      }
      catch (Exception e) {
        throw new RuntimeException("Failed to load task manager", e);
      }
    }
    else {
      result = DefaultTaskManager.get();
    }
    return result;
  }
}
