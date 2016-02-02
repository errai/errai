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

package org.jboss.errai.bus.client.tests;

import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.bus.client.api.ClientMessageBus;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.common.client.api.tasks.ClientTaskManager;
import org.jboss.errai.common.client.api.tasks.TaskManager;
import org.jboss.errai.common.client.api.tasks.TaskManagerFactory;
import org.jboss.errai.common.client.api.tasks.TaskManagerProvider;
import org.jboss.errai.common.client.logging.LoggingHandlerConfigurator;
import org.jboss.errai.common.client.logging.handlers.ErraiSystemLogHandler;

import com.google.gwt.junit.client.GWTTestCase;

/**
 * Base test class for testing ErraiBus-based code. Located in the main distribution so it can be extended
 * by other modules.
 */
public abstract class AbstractErraiTest extends GWTTestCase {
  protected static ClientMessageBus bus;

  static {
    System.out.println("REMEMBER! Bus tests will not succeed if: \n" +
        "1. You do not run the unit tests with the flag: -Dorg.jboss.errai.bus.do_long_poll=false \n" +
        "2. You do not have the main and test source directories in the runtime classpath");

  }

  @Override
  protected void gwtSetUp() throws Exception {
    // Only setup handlers for first test.
    if (LoggingHandlerConfigurator.get() == null) {
      // Cannot use console logger in non-production compiled tests.
      new LoggingHandlerConfigurator().onModuleLoad();
      final Handler[] handlers = Logger.getLogger("").getHandlers();
      for (final Handler handler : handlers) {
        handler.setLevel(Level.OFF);
      }
      LoggingHandlerConfigurator.get().getHandler(ErraiSystemLogHandler.class).setLevel(Level.ALL);
    }

    bus = (ClientMessageBus) ErraiBus.get();

    InitVotes.setTimeoutMillis(60000);

    if (!(TaskManagerFactory.get() instanceof ClientTaskManager)) {
      TaskManagerFactory.setTaskManagerProvider(new TaskManagerProvider() {
        private final ClientTaskManager clientTaskManager = new ClientTaskManager();

        @Override
        public TaskManager get() {
          return clientTaskManager;
        }
      });
    }
    InitVotes.startInitPolling();
  }

  @Override
  protected void gwtTearDown() throws Exception {
    bus.stop(true);
    InitVotes.reset();
  }

  /**
   * Invokes the given Runnable after the bus has finished initializing (it's
   * online and connected to the server). The test timeout is 45 seconds.
   * <p/>
   * You must call {@link #finishTest()} within your runnable, or the test will
   * time out.
   *
   * @param r
   *     the stuff to run once the bus is online.
   */
  protected void runAfterInit(final Runnable r) {
    runAfterInit(45000, r);
  }

  /**
   * Invokes the given Runnable after the bus has finished initializing (it's
   * online and connected to the server).
   * <p/>
   * You must call {@link #finishTest()} within your runnable before the given
   * timeout has elapsed, or the test will fail with a time out.
   *
   * @param r
   *          the stuff to run once the bus is online.
   */
  protected void runAfterInit(int timeoutMillis, final Runnable r) {
    delayTestFinish(timeoutMillis);
    InitVotes.registerOneTimeInitCallback(r);
  }

}
