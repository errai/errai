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

package org.jboss.errai.ioc.support.bus.tests.client;

import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.support.bus.tests.client.res.LifecycleBean;

/**
 * @author Mike Brock
 */
public class LifecycleExtensionsTest extends AbstractErraiIOCBusTest {
  public void testLifeCycleBeanWired() {
    runAfterInit(new Runnable() {
      @Override
      public void run() {
        LifecycleBean lifecycleBean = IOC.getBeanManager()
            .lookupBean(LifecycleBean.class).getInstance();

        assertNotNull("LifecycleBean is null", lifecycleBean);
        assertNotNull("Ballot was not injected into LifecycleBean", lifecycleBean.getBallot());
        assertTrue("AfterInitialization method was not called", lifecycleBean.isAfterInitCalled());
        finishTest();
      }
    });

  }
}
