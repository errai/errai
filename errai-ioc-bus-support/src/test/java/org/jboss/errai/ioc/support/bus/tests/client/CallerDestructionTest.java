/**
 * Copyright (C) 2018 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.common.client.api.ErrorCallback;
import org.jboss.errai.common.client.framework.RemoteServiceProxyFactory;
import org.jboss.errai.ioc.client.IOCUtil;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;
import org.jboss.errai.ioc.support.bus.tests.client.res.CalledService;
import org.jboss.errai.ioc.support.bus.tests.client.res.CallerTestModule;
import org.jboss.errai.ioc.support.bus.tests.client.res.MockCalledService;
import org.jboss.errai.ioc.support.bus.tests.client.res.MockCalledService.Mode;

/**
 * @author Max Barkley <mbarkley@redhat.com>
 */
public class CallerDestructionTest extends AbstractErraiIOCTest {

  private MockCalledService mockedProxy;
  private CallerTestModule module;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.support.bus.tests.BusIOCSupportTests";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    mockedProxy = new MockCalledService();
    RemoteServiceProxyFactory.addRemoteProxy(CalledService.class, () -> mockedProxy);
    super.gwtSetUp();
    module = IOCUtil.getInstance(CallerTestModule.class);
  }

  public void testRemoteCallbackNotInvokedForDisposedCaller() throws Exception {
    mockedProxy.setMode(Mode.REMOTE);
    IOCUtil.destroy(module);

    module.caller.call(retVal -> fail("Should not invoke remote callback after bean destroyed.")).method();
  }

  public void testErrorCallbackNotInvokedForDisposedCaller() throws Exception {
    mockedProxy.setMode(Mode.ERROR);
    IOCUtil.destroy(module);

    module.caller.call(
            retVal -> fail(
                    "Something wrong with test setup. Shouldn't ever call remote callback when testing error callback."),
            (ErrorCallback<?>) (msg, t) -> {
              fail("Should not invoke error callback after bean is destroyed.");
              return false;
            }).method();
  }

  public void testRemoteCallbacksNotInvokedForDisposedBatchCaller() throws Exception {
    mockedProxy.setMode(Mode.BATCH_REMOTE);
    module.batchCaller.call(retVal -> fail("Should not invoke first remote callback."), CalledService.class).method();
    module.batchCaller.call(retVal -> fail("Should not invoke second remote callback."), CalledService.class).method();
    IOCUtil.destroy(module);

    module.batchCaller.sendBatch(r -> fail("Should not invoke success callback."));
  }

  public void testErrorCallbacksNotInvokedForDisposedBatchCaller() throws Exception {
    mockedProxy.setMode(Mode.BATCH_ERROR);
    module.batchCaller.call(retVal -> fail("Should not invoke first remote callback."),
                            (msg, error) -> {
                              fail("Should not invoke first error callback.");
                              return true;
                            },
                            CalledService.class).method();
    module.batchCaller.call(retVal -> fail("Should not invoke second remote callback."),
                            (msg, error) -> {
                              fail("Should not invoke second error callback.");
                              return true;
                            },
                            CalledService.class).method();
    IOCUtil.destroy(module);

    module.batchCaller.sendBatch(r -> fail("Should not invoke remote callback when testing error callbacks."),
                                      (msg, error) -> {
                                        fail("Should not invoke failure callback.");
                                        return true;
                                      });
  }

}
