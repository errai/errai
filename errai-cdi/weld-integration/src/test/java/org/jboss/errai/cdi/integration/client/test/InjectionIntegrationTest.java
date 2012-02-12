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

package org.jboss.errai.cdi.integration.client.test;

import org.jboss.errai.bus.client.api.RemoteCallback;
import org.jboss.errai.cdi.integration.client.RpcTestBean;
import org.jboss.errai.cdi.integration.client.eg.BeanA;
import org.jboss.errai.cdi.integration.client.eg.BeanB;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * @author Mike Brock
 */
public class InjectionIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.InjectionTests";
  }


  public void testDependentBeanScope() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        BeanA beanA = BeanA.getInstance();

        BeanB b1 = beanA.getBean1();
        BeanB b2 = beanA.getBean2();
        BeanB b3 = beanA.getBean3();

        assertTrue(b2.getInstance() > b1.getInstance());
        assertTrue(b3.getInstance() > b2.getInstance());

        finishTest();
      }
    });

    delayTestFinish(60000);
  }

}