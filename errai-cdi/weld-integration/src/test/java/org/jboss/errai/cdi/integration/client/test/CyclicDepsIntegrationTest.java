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

import org.jboss.errai.cdi.integration.client.shared.BeanInjectSelf;
import org.jboss.errai.cdi.integration.client.shared.ConsumerBeanA;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * @author Mike Brock
 */
public class CyclicDepsIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.InjectionTestModule";
  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testBeanInjectsIntoSelf() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        BeanInjectSelf beanA = IOC.getBeanManager()
                .lookupBean(BeanInjectSelf.class).getInstance();

        assertNotNull(beanA);
        assertNotNull(beanA.getSelf());
        assertEquals(beanA.getInstance(), beanA.getSelf().getInstance());

        finishTest();
      }
    });
  }

  public void testCycleOnProducerBean() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        ConsumerBeanA consumerBeanA = IOC.getBeanManager()
                .lookupBean(ConsumerBeanA.class).getInstance();

        assertNotNull(consumerBeanA);
        assertNotNull(consumerBeanA.getFoo());
        assertEquals("barz", consumerBeanA.getFoo().getName());
        assertNotNull(consumerBeanA.getProducerBeanA());
        assertNotNull(consumerBeanA.getProducerBeanA().getConsumerBeanA());
        assertEquals("barz", consumerBeanA.getProducerBeanA().getConsumerBeanA().getFoo().getName());

        finishTest();
      }
    });

  }
}