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

package org.jboss.errai.cdi.async.test.postconstruct.client;

import org.jboss.errai.cdi.async.test.postconstruct.client.res.PostConstrAppBean;
import org.jboss.errai.cdi.async.test.postconstruct.client.res.PostConstrBeanA;
import org.jboss.errai.cdi.async.test.postconstruct.client.res.PostConstrBeanB;
import org.jboss.errai.cdi.async.test.postconstruct.client.res.PostConstrBeanC;
import org.jboss.errai.cdi.async.test.postconstruct.client.res.PostConstructTestUtil;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;

import java.util.List;

/**
 * @author Mike Brock
 */
public class AsyncPostConstructOrderTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.async.test.postconstruct.PostConstructOrderTests";
  }

  public void testPostConstructFiresInCorrectOrderR() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        PostConstructTestUtil.reset();
        IOC.getAsyncBeanManager().lookupBean(PostConstrBeanA.class)
            .getInstance(new CreationalCallback<PostConstrBeanA>() {
              @Override
              public void callback(PostConstrBeanA beanA) {
                assertNotNull("PostConstrBeanA was not resolved", beanA);

                final List<String> postConstructOrder = PostConstructTestUtil.getOrderOfFiring();

                assertEquals(PostConstrBeanC.class.getName(), postConstructOrder.get(0));
                assertEquals(PostConstrBeanB.class.getName(), postConstructOrder.get(1));
                assertEquals(PostConstrBeanA.class.getName(), postConstructOrder.get(2));

                finishTest();
              }
            });
      }
    });
  }

  public void testPostConstructCalledAsDynamicLookup() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        PostConstructTestUtil.reset();

        IOC.getAsyncBeanManager().lookupBean(PostConstrAppBean.class)
            .getInstance(new CreationalCallback<PostConstrAppBean>() {
              @Override
              public void callback(PostConstrAppBean beanInstance) {
                assertTrue(beanInstance.isFinished());

                finishTest();
              }
            });
      }
    });
  }
}
