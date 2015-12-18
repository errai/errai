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

package org.jboss.errai.cdi.async.test.cyclic.client;

import static org.jboss.errai.ioc.client.container.IOC.getAsyncBeanManager;

import org.jboss.errai.cdi.async.test.cyclic.client.res.ApplicationScopedBeanInjectSelf;
import org.jboss.errai.cdi.async.test.cyclic.client.res.Car;
import org.jboss.errai.cdi.async.test.cyclic.client.res.ConsumerBeanA;
import org.jboss.errai.cdi.async.test.cyclic.client.res.CycleNodeA;
import org.jboss.errai.cdi.async.test.cyclic.client.res.EquHashCheckCycleA;
import org.jboss.errai.cdi.async.test.cyclic.client.res.EquHashCheckCycleB;
import org.jboss.errai.cdi.async.test.cyclic.client.res.Petrol;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanFuture;
import org.jboss.errai.ioc.client.container.async.AsyncBeanQuery;

/**
 * @author Mike Brock
 */
public class AsyncCyclicDepsIntegrationTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.async.test.cyclic.AsyncCyclicIntegrationTest";
  }

  public void testBasicDependencyCycle() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        getAsyncBeanManager().lookupBean(CycleNodeA.class)
            .getInstance(new CreationalCallback<CycleNodeA>() {
              @Override
              public void callback(final CycleNodeA nodeA) {

                assertNotNull(nodeA);
                assertNotNull(nodeA.getCycleNodeB());
                assertNotNull(nodeA.getCycleNodeB().getCycleNodeA());
                assertNotNull(nodeA.getCycleNodeB().getCycleNodeC());
                assertNotNull(nodeA.getCycleNodeB().getCycleNodeC().getCycleNodeA());
                assertEquals("CycleNodeA is a different instance at different points in the graph",
                    nodeA.getNodeId(), nodeA.getCycleNodeB().getCycleNodeC().getCycleNodeA().getNodeId());
                assertEquals("CycleNodeA is a different instance at different points in the graph",
                    nodeA.getNodeId(), nodeA.getCycleNodeB().getCycleNodeA().getNodeId());

                finishTest();
              }
            });
      }
    });
  }

  public void testCircularInjectionOnOneNormalAndOneDependentBean() throws Exception {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanQuery asyncBeanQuery = new AsyncBeanQuery();
        final AsyncBeanFuture<Petrol> petrolFuture = asyncBeanQuery.load(Petrol.class);
        final AsyncBeanFuture<Car> carFuture = asyncBeanQuery.load(Car.class);

        asyncBeanQuery.query(new Runnable() {
          @Override
          public void run() {
            final Petrol petrol = petrolFuture.get();
            final Car car = carFuture.get();

            assertEquals(petrol.getNameOfCar(), car.getName());
            assertEquals(car.getNameOfPetrol(), petrol.getName());
            finishTest();
          }
        });
      }
    });
  }

  public void testCyclingBeanDestroy() {
    asyncTest(new Runnable() {
      @Override
      public void run() {

        getAsyncBeanManager().lookupBean(ApplicationScopedBeanInjectSelf.class)
            .getInstance(new CreationalCallback<ApplicationScopedBeanInjectSelf>() {
              @Override
              public void callback(final ApplicationScopedBeanInjectSelf beanA) {

                assertNotNull(beanA);
                assertNotNull(beanA.getSelf());

                getAsyncBeanManager().destroyBean(beanA);

                assertFalse("bean should no longer be managed", getAsyncBeanManager().isManaged(beanA));

                finishTest();
              }
            });
      }
    });
  }

  public void testCyclingBeanDestroyViaProxy() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        getAsyncBeanManager().lookupBean(ApplicationScopedBeanInjectSelf.class)
            .getInstance(new CreationalCallback<ApplicationScopedBeanInjectSelf>() {
              @Override
              public void callback(ApplicationScopedBeanInjectSelf beanA) {
                assertNotNull(beanA);
                assertNotNull(beanA.getSelf());

                // destroy via the proxy reference through self
                getAsyncBeanManager().destroyBean(beanA.getSelf());

                assertFalse("bean should no longer be managed", getAsyncBeanManager().isManaged(beanA));

                finishTest();
              }
            });
      }
    });
  }

  public void testBeanInjectsIntoSelf() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        getAsyncBeanManager().lookupBean(ApplicationScopedBeanInjectSelf.class)
            .getInstance(new CreationalCallback<ApplicationScopedBeanInjectSelf>() {
              @Override
              public void callback(final ApplicationScopedBeanInjectSelf beanA) {

                assertNotNull(beanA);
                assertNotNull(beanA.getSelf());
                assertEquals(beanA.getInstance(), beanA.getSelf().getInstance());

                assertTrue("bean.self should be a proxy", getAsyncBeanManager().isProxyReference(beanA.getSelf()));
            assertSame("unwrapped proxy should be the same as outer instance",
                    getAsyncBeanManager().getActualBeanReference(beanA),
                    getAsyncBeanManager().getActualBeanReference(beanA.getSelf()));

                finishTest();
              }
            });
      }
    });
  }

  public void testCycleOnProducerBeans() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        getAsyncBeanManager().lookupBean(ConsumerBeanA.class)
            .getInstance(new CreationalCallback<ConsumerBeanA>() {
              @Override
              public void callback(final ConsumerBeanA consumerBeanA) {
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

                finishTest();
              }
            });
      }
    });
  }

  public void testHashcodeAndEqualsWorkThroughProxies() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        AsyncBeanQuery asyncBeanQuery = new AsyncBeanQuery();
        final AsyncBeanFuture<EquHashCheckCycleA> equHashCheckCycleAFuture
            = asyncBeanQuery.load(getAsyncBeanManager().lookupBean(EquHashCheckCycleA.class));

        final AsyncBeanFuture<EquHashCheckCycleB> equHashCheckCycleBFuture
            = asyncBeanQuery.load(getAsyncBeanManager().lookupBean(EquHashCheckCycleB.class));

        asyncBeanQuery.query(new Runnable() {
          @Override
          public void run() {
            final EquHashCheckCycleA equHashCheckCycleA = equHashCheckCycleAFuture.get();
            final EquHashCheckCycleB equHashCheckCycleB = equHashCheckCycleBFuture.get();

            assertNotNull(equHashCheckCycleA);
            assertNotNull(equHashCheckCycleB);

            assertTrue("at least one bean should be proxied",
                IOC.getAsyncBeanManager().isProxyReference(equHashCheckCycleA.getEquHashCheckCycleB())
                    || IOC.getAsyncBeanManager().isProxyReference(equHashCheckCycleB.getEquHashCheckCycleA()));

            assertEquals("equals contract broken", equHashCheckCycleA, equHashCheckCycleB.getEquHashCheckCycleA());
            assertEquals("equals contract broken", equHashCheckCycleB, equHashCheckCycleA.getEquHashCheckCycleB());

            assertEquals("hashCode contract broken", equHashCheckCycleA.hashCode(),
                equHashCheckCycleB.getEquHashCheckCycleA().hashCode());

            assertEquals("hashCode contract broken", equHashCheckCycleB.hashCode(),
                equHashCheckCycleA.getEquHashCheckCycleB().hashCode());

            finishTest();
          }
        });

      }
    });

  }
}
