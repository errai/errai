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
import org.jboss.errai.cdi.integration.client.shared.CycleNodeA;
import org.jboss.errai.cdi.integration.client.shared.DependentBeanInjectSelf;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
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

  public void testBasicDependencyCycle() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        CycleNodeA nodeA = IOC.getBeanManager()
                .lookupBean(CycleNodeA.class).getInstance();

        assertNotNull(nodeA);
        assertNotNull(nodeA.getCycleNodeB());
        assertNotNull(nodeA.getCycleNodeB().getCycleNodeC());
        assertNotNull(nodeA.getCycleNodeB().getCycleNodeC().getCycleNodeA());
        assertEquals("CycleNodeA is a different instance at different points in the graph",
                nodeA.getNodeId(), nodeA.getCycleNodeB().getCycleNodeC().getCycleNodeA().getNodeId());

        finishTest();
      }
    });

  }

  public void testBeanInjectsIntoSelf() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
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


  public void testDependentBeanInjectsIntoSelf() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        DependentBeanInjectSelf beanA = IOC.getBeanManager()
                .lookupBean(DependentBeanInjectSelf.class).getInstance();

        assertNotNull(beanA);
        assertNotNull(beanA.getSelf());
        assertEquals(beanA.getInstance(), beanA.getSelf().getInstance());

        finishTest();
      }
    });
  }

  public void testCycleOnProducerBeans() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
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

        assertNotNull(consumerBeanA.getBar());
        assertEquals("fooz", consumerBeanA.getBar().getName());
        assertNotNull(consumerBeanA.getProducerBeanA().getConsumerBeanA().getBar());
        assertEquals("fooz", consumerBeanA.getProducerBeanA().getConsumerBeanA().getBar().getName());

        finishTest();
      }
    });
  }

}