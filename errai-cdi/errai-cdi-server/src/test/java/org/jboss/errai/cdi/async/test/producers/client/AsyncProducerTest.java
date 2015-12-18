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

package org.jboss.errai.cdi.async.test.producers.client;

import java.util.List;

import org.jboss.errai.cdi.async.test.producers.client.res.AsyncProducerDependentBean;
import org.jboss.errai.cdi.async.test.producers.client.res.AsyncSingletonProducerDependentBean;
import org.jboss.errai.cdi.async.test.producers.client.res.AustenProducerDependnetBean;
import org.jboss.errai.cdi.async.test.producers.client.res.BeanConstrConsumesOwnProducer;
import org.jboss.errai.cdi.async.test.producers.client.res.DepBeanProducerConsumer;
import org.jboss.errai.cdi.async.test.producers.client.res.Fooblie;
import org.jboss.errai.cdi.async.test.producers.client.res.FooblieDependentBean;
import org.jboss.errai.cdi.async.test.producers.client.res.FooblieMaker;
import org.jboss.errai.cdi.async.test.producers.client.res.PseudoBeanProducerConsumer;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanFuture;
import org.jboss.errai.ioc.client.container.async.AsyncBeanQuery;

/**
 * @author Mike Brock
 */
public class AsyncProducerTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.async.test.producers.AsyncProducerTest";
  }

  public void testProducer() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(AsyncProducerDependentBean.class)
            .getInstance(new CreationalCallback<AsyncProducerDependentBean>() {
              @Override
              public void callback(final AsyncProducerDependentBean bean) {
                assertNotNull(bean);

                assertNotNull(bean.getMaBean());
                assertNotNull(bean.getMaBean2());

                assertNotSame(bean.getMaBean(), bean.getMaBean2());

                finishTest();
              }
            });
      }
    });
  }

  public void testSingletonProducer() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(AsyncSingletonProducerDependentBean.class)
            .getInstance(new CreationalCallback<AsyncSingletonProducerDependentBean>() {
              @Override
              public void callback(final AsyncSingletonProducerDependentBean bean) {
                assertNotNull(bean);

                assertNotNull(bean.getLaBean());
                assertNotNull(bean.getLaBean2());

                assertSame(bean.getLaBean() + " != " + bean.getLaBean2(), bean.getLaBean(), bean.getLaBean2());

                finishTest();
              }
            });
      }
    });
  }

  public void testBeanConstrConsumesOwnProduer() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(BeanConstrConsumesOwnProducer.class)
            .getInstance(new CreationalCallback<BeanConstrConsumesOwnProducer>() {
              @Override
              public void callback(final BeanConstrConsumesOwnProducer bean) {
                assertNotNull(bean);
                assertNotNull(bean.getWrappedKitten());
                assertNotNull(bean.getWrappedKitten().getKitten());

                finishTest();
              }
            });
      }
    });
  }

  public void testProducerFromBeanIntoBean() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(BeanConstrConsumesOwnProducer.class)
            .getInstance(new CreationalCallback<BeanConstrConsumesOwnProducer>() {
              @Override
              public void callback(final BeanConstrConsumesOwnProducer bean) {
                assertNotNull(bean);
                assertNotNull(bean.getWrappedKitten());
                assertNotNull(bean.getWrappedKitten().getKitten());

                finishTest();
              }
            });
      }
    });
  }

  public void testProducerAcceptingInterfaceType() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(AustenProducerDependnetBean.class)
            .getInstance(new CreationalCallback<AustenProducerDependnetBean>() {
              @Override
              public void callback(final AustenProducerDependnetBean bean) {
                assertNotNull(bean);
                assertNotNull(bean.getAusten());

                finishTest();
              }
            });
      }
    });
  }

  public void testDisposerMethod() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanQuery beanQuery = new AsyncBeanQuery();
        final AsyncBeanFuture<FooblieMaker> makerFuture = beanQuery.load(FooblieMaker.class);
        final AsyncBeanFuture<FooblieDependentBean> fooblieDependentFuture1 = beanQuery.load(FooblieDependentBean.class);
        final AsyncBeanFuture<FooblieDependentBean> fooblieDependentFuture2 = beanQuery.load(FooblieDependentBean.class);

        beanQuery.query(new Runnable() {
          @Override
          public void run() {
            final FooblieMaker maker = makerFuture.get();
            final FooblieDependentBean fooblieDependentBean1 = fooblieDependentFuture1.get();
            final FooblieDependentBean fooblieDependentBean2 = fooblieDependentFuture2.get();

            IOC.getAsyncBeanManager().destroyBean(fooblieDependentBean1);
            IOC.getAsyncBeanManager().destroyBean(fooblieDependentBean2);

            final List<Fooblie> foobliesResponse = maker.getDestroyedFoobliesResponse();

            assertEquals("there should be two destroyed beans", 2, foobliesResponse.size());
            assertEquals(fooblieDependentBean1.getFooblieResponse(), foobliesResponse.get(0));
            assertEquals(fooblieDependentBean2.getFooblieResponse(), foobliesResponse.get(1));

            final List<Fooblie> foobliesGreets = maker.getDestroyedFoobliesGreets();

            assertEquals("there should be two destroyed beans", 2, foobliesGreets.size());
            assertEquals(fooblieDependentBean1.getFooblieGreets(), foobliesGreets.get(0));
            assertEquals(fooblieDependentBean2.getFooblieGreets(), foobliesGreets.get(1));

            final List<Fooblie> foobliesParts = maker.getDestroyedFoobliesParts();

            assertEquals("there should be two destroyed beans", 2, foobliesParts.size());
            assertEquals(fooblieDependentBean1.getFooblieParts(), foobliesParts.get(0));
            assertEquals(fooblieDependentBean2.getFooblieParts(), foobliesParts.get(1));

            finishTest();
          }
        });
      }
    });
  }

  public void testNormalDependentProducer() throws Exception {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(DepBeanProducerConsumer.class)
        .getInstance(new CreationalCallback<DepBeanProducerConsumer>() {
          @Override
          public void callback(DepBeanProducerConsumer bean) {
            assertNotNull(bean.getProducable());
            finishTest();
          }
        });
      }
    });
  }

  public void testPseudoProducer() throws Exception {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(PseudoBeanProducerConsumer.class)
        .getInstance(new CreationalCallback<PseudoBeanProducerConsumer>() {
          @Override
          public void callback(PseudoBeanProducerConsumer bean) {
            assertNotNull(bean.getProducable());
            finishTest();
          }
        });
      }
    });
  }
}
