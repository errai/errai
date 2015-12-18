/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.ioc.client.container;

import com.google.gwt.core.client.GWT;

/**
 * A utility class for performing lifecycle operations on the bean manager. In general, you should <em>NEVER</em>
 * use this class in your application. It is used directly by the code generator to ensure the bean manager initializes
 * in a consistent state when it is reset within the same client instance.
 *
 * @author Mike Brock
 */
public class IOCBeanManagerLifecycle {
  /**
   * Resets the bean manager by cleanly destroying all beans and taking them out of service.
   */
  public void resetBeanManager() {
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

    if (iocEnvironment.isAsync()) {
      IOC.getAsyncBeanManager().destroyAllBeans();
    }
    else {
      IOC.getBeanManager().destroyAllBeans();
    }
  }
}
