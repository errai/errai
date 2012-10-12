/*
 * Copyright 2011 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.client.container;

import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;

/**
 * A simple utility class which provides a static reference in the client to the bean manager.
 *
 * @author Mike Brock
 */
public final class IOC {
  private static final IOC inst = new IOC();

  private final IOCBeanManager beanManager;
  private final AsyncBeanManager asyncBeanManager;

  private IOC() {
    if (IOCEnvironment.isAsync()) {
      asyncBeanManager = new AsyncBeanManager();
      beanManager = null;
    }
    else {
      beanManager = new IOCBeanManager();
      asyncBeanManager = null;
    }
  }

  /**
   * Returns a reference to the bean manager in the client.
   *
   * @return the singleton instance of the client bean manager.
   *
   * @see IOCBeanManager
   */
  public static IOCBeanManager getBeanManager() {
    if (inst.beanManager == null) {
      throw new RuntimeException("the bean manager has been initialized in async mode. " +
          "You must use getAsyncBeanManager()");
    }
    return inst.beanManager;
  }

  public static AsyncBeanManager getAsyncBeanManager() {
    if (inst.asyncBeanManager == null) {
      throw new RuntimeException("the bean manager has been initialized in synchronous mode. " +
          "You must use getBeanManager()");
    }

    return inst.asyncBeanManager;
  }
}
