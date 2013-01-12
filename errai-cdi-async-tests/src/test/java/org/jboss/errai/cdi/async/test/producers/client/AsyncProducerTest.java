package org.jboss.errai.cdi.async.test.producers.client;

import org.jboss.errai.cdi.async.test.producers.client.res.AsyncProducerDependentBean;
import org.jboss.errai.cdi.async.test.producers.client.res.AsyncSingletonProducerDependentBean;
import org.jboss.errai.cdi.async.test.producers.client.res.BeanConstrConsumesOwnProducer;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;

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

  public void testProducerFromDependentBeanIntoDependentBean() {
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
}