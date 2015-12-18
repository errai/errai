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

package org.jboss.errai.ioc.client;

import java.util.ArrayList;
import java.util.List;

import org.jboss.errai.ioc.client.container.BeanManagerSetup;
import org.jboss.errai.ioc.client.container.ContextManager;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;

public class Container implements EntryPoint {

  private static final Logger logger = LoggerFactory.getLogger(Container.class);

  @Override
  public void onModuleLoad() {
    bootstrapContainer();
  }

  public void bootstrapContainer() {
    try {
      init = false;

      QualifierUtil.initFromFactoryProvider(new QualifierEqualityFactoryProvider() {
        @Override
        public QualifierEqualityFactory provide() {
          return GWT.create(QualifierEqualityFactory.class);
        }
      });


      final BeanManagerSetup beanManager;
      if (GWT.<IOCEnvironment>create(IOCEnvironment.class).isAsync()) {
        logger.info("bean manager initialized in async mode.");
        beanManager = (BeanManagerSetup) IOC.getAsyncBeanManager();
      } else {
        beanManager = (BeanManagerSetup) IOC.getBeanManager();
      }

      final Bootstrapper bootstrapper = GWT.create(Bootstrapper.class);
      final ContextManager contextManager = bootstrapper.bootstrapContainer();

      beanManager.setContextManager(contextManager);
      init = true;
      for (final Runnable run : afterInit) {
        run.run();
      }
      afterInit.clear();
      logger.info("IOC bootstrapper successfully initialized.");
    }
    catch (RuntimeException ex) {
      logger.error("critical error in IOC container bootstrap: " + ex.getClass().getName() + ": "
          + ex.getMessage());

      throw ex;
    }
  }

  private static final List<Runnable> afterInit = new ArrayList<Runnable>();
  private static boolean init = false;

  /**
   * Runs the specified {@link Runnable} only after the bean manager has fully initialized. It is generally not
   * necessary to use this method from within beans themselves. But if you are generated out-of-container calls
   * into the bean manager (such as for testing), it may be necessary to use this method to ensure that the beans
   * you wish to lookup have been loaded.
   * <p/>
   * Use of this method is really only necessary when using the bean manager in asynchronous mode as wiring of the
   * container synchronously does not yield during bootstrapping operations.
   * <p/>
   * If the bean manager is already initialized when you call this method, the <tt>Runnable</tt> is invoked immediately.
   *
   * @param runnable
   *     the {@link Runnable} to execute after bean manager initialization.
   */
  public static void runAfterInit(final Runnable runnable) {
    if (init) {
      runnable.run();
    } else {
      afterInit.add(runnable);
    }
  }

  /**
   * Short-alias method for {@link #runAfterInit(Runnable)}.
   *
   * @param runnable
   */
  public static void $(final Runnable runnable) {
    runAfterInit(runnable);
  }

  public static void reset() {
    init = false;
    afterInit.clear();
  }

}
