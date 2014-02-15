/*
 * Copyright 2012 JBoss, by Red Hat, Inc
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

package org.jboss.errai.ioc.async.test.decorator.client;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.async.test.decorator.client.res.MyDecoratedBean;
import org.jboss.errai.ioc.async.test.decorator.client.res.TestDataCollector;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class DecoratorAPITests extends AbstractErraiIOCTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.decorator.DecoratorAPITests";
  }

  public void testBeanDecoratedWithProxy() {
    $(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(MyDecoratedBean.class).getInstance(
            new CreationalCallback<MyDecoratedBean>() {
              @Override
              public void callback(MyDecoratedBean instance) {
                instance.someMethod("a", 1);
                instance.someMethod("b", 2);
                instance.someMethod("c", 3);

                assertEquals(instance.getTestMap(), TestDataCollector.getBeforeInvoke());
                assertEquals(instance.getTestMap(), TestDataCollector.getAfterInvoke());

                Map<String, Object> expectedProperties = new HashMap<String, Object>();
                expectedProperties.put("foobar", "foobie!");

                assertEquals(expectedProperties, TestDataCollector.getProperties());

                finishTest();
              }
            });
      }
    });
  }
}
