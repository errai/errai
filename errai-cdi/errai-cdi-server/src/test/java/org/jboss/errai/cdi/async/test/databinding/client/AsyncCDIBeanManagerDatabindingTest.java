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

package org.jboss.errai.cdi.async.test.databinding.client;

import org.jboss.errai.cdi.async.test.databinding.client.res.MyBean;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;

/**
 * @author Mike Brock
 */
public class AsyncCDIBeanManagerDatabindingTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.async.test.databinding.AsyncCDIDatabindingTest";
  }

  public void testModelInjectionWorksWithAsyncBeanManager() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanManager bm = IOC.getAsyncBeanManager();
        bm.lookupBean(MyBean.class)
            .getInstance(new CreationalCallback<MyBean>() {
              @Override
              public void callback(MyBean beanInstance) {
                assertNotNull(beanInstance.getModel());
                beanInstance.getModel().setName("foo");
                assertEquals("foo", beanInstance.getName().getValue());
                finishTest();
              }
            });
      }
    });
  }
}
