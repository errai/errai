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

package org.jboss.errai.cdi.injection.client.test;

import static org.jboss.errai.ioc.client.container.IOC.getBeanManager;

import org.jboss.errai.cdi.injection.client.BeanInjectSelf;
import org.jboss.errai.cdi.injection.client.Car;
import org.jboss.errai.cdi.injection.client.ConsumerBeanA;
import org.jboss.errai.cdi.injection.client.CycleNodeA;
import org.jboss.errai.cdi.injection.client.DependentBeanInjectSelf;
import org.jboss.errai.cdi.injection.client.EquHashCheckCycleA;
import org.jboss.errai.cdi.injection.client.EquHashCheckCycleB;
import org.jboss.errai.cdi.injection.client.Petrol;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;

/**
 * @author Mike Brock
 */
public class CyclicDepsIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  @Override
  protected void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testBasicDependencyCycle() {

    CycleNodeA nodeA = getBeanManager()
            .lookupBean(CycleNodeA.class).getInstance();

    assertNotNull(nodeA);
    assertNotNull(nodeA.getCycleNodeB());
    assertNotNull(nodeA.getCycleNodeB().getCycleNodeA());
    assertNotNull(nodeA.getCycleNodeB().getCycleNodeC());
    assertNotNull(nodeA.getCycleNodeB().getCycleNodeC().getCycleNodeA());
    assertEquals("CycleNodeA is a different instance at different points in the graph",
            nodeA.getNodeId(), nodeA.getCycleNodeB().getCycleNodeC().getCycleNodeA().getNodeId());
    assertEquals("CycleNodeA is a different instance at different points in the graph",
            nodeA.getNodeId(), nodeA.getCycleNodeB().getCycleNodeA().getNodeId());
  }

  public void testCircularInjectionOnOneNormalAndOneDependentBean() throws Exception {
    Petrol petrol = getBeanManager().lookupBean(Petrol.class).getInstance();
    Car car = getBeanManager().lookupBean(Car.class).getInstance();
    assertEquals(petrol.getNameOfCar(), car.getName());
    assertEquals(car.getNameOfPetrol(), petrol.getName());
  }

  public void testBeanInjectsIntoSelf() {
    BeanInjectSelf beanA = getBeanManager()
            .lookupBean(BeanInjectSelf.class).getInstance();

    assertNotNull(beanA);
    assertNotNull(beanA.getSelf());
    assertEquals(beanA.getInstance(), beanA.getSelf().getInstance());

    assertTrue("bean.self should be a proxy", getBeanManager().isProxyReference(beanA.getSelf()));
    assertSame("unwrapped proxy should be the same as outer instance", beanA,
            getBeanManager().getActualBeanReference(beanA.getSelf()));
  }

  public void testCyclingBeanDestroy() {
    DependentBeanInjectSelf beanA = getBeanManager()
            .lookupBean(DependentBeanInjectSelf.class).getInstance();

    assertNotNull(beanA);
    assertNotNull(beanA.getSelf());

    getBeanManager().destroyBean(beanA);

    assertFalse("bean should no longer be managed", getBeanManager().isManaged(beanA));
    assertFalse("bean.self should no longer be recognized as proxy",
            getBeanManager().isProxyReference(beanA.getSelf()));
  }

  public void testCyclingBeanDestroyViaProxy() {

    DependentBeanInjectSelf beanA = getBeanManager()
            .lookupBean(DependentBeanInjectSelf.class).getInstance();

    assertNotNull(beanA);
    assertNotNull(beanA.getSelf());

    // destroy via the proxy reference through self
    getBeanManager().destroyBean(beanA.getSelf());

    assertFalse("bean should no longer be managed", getBeanManager().isManaged(beanA));
    assertFalse("bean.self should no longer be recognized as proxy",
            getBeanManager().isProxyReference(beanA.getSelf()));

  }

  public void testDependentBeanInjectsIntoSelf() {

    DependentBeanInjectSelf beanA = getBeanManager()
            .lookupBean(DependentBeanInjectSelf.class).getInstance();

    assertNotNull(beanA);
    assertNotNull(beanA.getSelf());
    assertEquals(beanA.getInstance(), beanA.getSelf().getInstance());

    assertTrue("bean.self should be a proxy", getBeanManager().isProxyReference(beanA.getSelf()));
    assertSame("unwrapped proxy should be the same as outer instance", beanA, getBeanManager()
            .getActualBeanReference(beanA.getSelf()));

  }

  public void testCycleOnProducerBeans() {

    ConsumerBeanA consumerBeanA = getBeanManager()
            .lookupBean(ConsumerBeanA.class).getInstance();

    assertNotNull(consumerBeanA);
    assertNotNull("foo was not injected", consumerBeanA.getFoo());
    assertNotNull("baz was not inject", consumerBeanA.getBaz());

    assertEquals("barz", consumerBeanA.getFoo().getName());

    assertNotNull(consumerBeanA.getProducerBeanA());
    assertNotNull(consumerBeanA.getProducerBeanA().getConsumerBeanA());
    assertEquals("barz", consumerBeanA.getProducerBeanA().getConsumerBeanA().getFoo().getName());

    assertNotNull(consumerBeanA.getBar());
    assertEquals("fooz", consumerBeanA.getBar().getName());
    assertNotNull(consumerBeanA.getProducerBeanA().getConsumerBeanA().getBar());
    assertEquals("fooz", consumerBeanA.getProducerBeanA().getConsumerBeanA().getBar().getName());
  }

  public void testHashcodeAndEqualsWorkThroughProxies() {

    EquHashCheckCycleA equHashCheckCycleA = getBeanManager()
            .lookupBean(EquHashCheckCycleA.class).getInstance();

    EquHashCheckCycleB equHashCheckCycleB = getBeanManager()
            .lookupBean(EquHashCheckCycleB.class).getInstance();

    assertNotNull(equHashCheckCycleA);
    assertNotNull(equHashCheckCycleB);

    assertEquals("equals contract broken", equHashCheckCycleA, equHashCheckCycleB.getEquHashCheckCycleA());
    assertEquals("equals contract broken", equHashCheckCycleB, equHashCheckCycleA.getEquHashCheckCycleB());

    assertEquals("hashCode contract broken", equHashCheckCycleA.hashCode(),
            equHashCheckCycleB.getEquHashCheckCycleA().hashCode());

    assertEquals("hashCode contract broken", equHashCheckCycleB.hashCode(),
            equHashCheckCycleA.getEquHashCheckCycleB().hashCode());

  }
}