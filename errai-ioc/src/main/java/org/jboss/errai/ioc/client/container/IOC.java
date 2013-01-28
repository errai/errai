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

import com.google.gwt.core.client.GWT;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManagerImpl;

/**
 * A simple utility class which provides a static reference in the client to the bean manager.
 *
 * @author Mike Brock
 */
public final class IOC {
  private static final IOC inst = new IOC();
  private final ClientBeanManager beanManager;

  private IOC() {
    IOCEnvironment iocEnvironment;

    try {
      iocEnvironment = GWT.create(IOCEnvironment.class);
    }
    catch (UnsupportedOperationException e) {
      iocEnvironment = new IOCEnvironment() {
        @Override
        public boolean isAsync() {
          return false;
        }

        @Override
        public ClientBeanManager getNewBeanManager() {
          if (!GWT.isClient()) {
           return new SyncBeanManagerImpl();
          }
          else {
            return null;
          }
        }
      };
    }

    beanManager = iocEnvironment.getNewBeanManager();
  }

  /**
   * Returns a reference to the bean manager in the client.
   *
   * @return the singleton instance of the client bean manager.
   *
   * @see SyncBeanManagerImpl
   */
  public static SyncBeanManager getBeanManager() {
    if (inst.beanManager instanceof AsyncBeanManager) {
      throw new RuntimeException("the bean manager has been initialized in async mode. " +
          "You must use getAsyncBeanManager()");
    }
    return (SyncBeanManagerImpl) inst.beanManager;
  }

  public static AsyncBeanManager getAsyncBeanManager() {
    if (inst.beanManager instanceof SyncBeanManager) {
      throw new RuntimeException("the bean manager has been initialized in synchronous mode. " +
          "You must use getBeanManager()");
    }

    return (AsyncBeanManager) inst.beanManager;
  }
}
