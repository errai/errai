package org.jboss.errai.cdi.async.test.producers.client;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.cdi.async.test.producers.client.res.AsyncProducerDependentBean;
import org.jboss.errai.cdi.async.test.producers.client.res.AsyncSingletonProducerDependentBean;
import org.jboss.errai.cdi.async.test.producers.client.res.BeanConstrConsumesOwnProducer;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCEnvironment;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;

import java.sql.Time;

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
    delayTestFinish(10000);

    new Timer() {
      @Override
      public void run() {

        IOC.getAsyncBeanManager().lookupBean(AsyncProducerDependentBean.class)
            .getInstance(new CreationalCallback<AsyncProducerDependentBean>() {
              @Override
              public void callback(final AsyncProducerDependentBean beanInstance) {
                assertNotNull(beanInstance);

                assertNotNull(beanInstance.getMaBean());
                assertNotNull(beanInstance.getMaBean2());

                assertNotSame(beanInstance.getMaBean(), beanInstance.getMaBean2());

                finishTest();
              }
            });
      }
    }.schedule(100);

  }

  public void testSingletonProducer() {
    delayTestFinish(10000);

    new Timer() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(AsyncSingletonProducerDependentBean.class)
            .getInstance(new CreationalCallback<AsyncSingletonProducerDependentBean>() {
              @Override
              public void callback(final AsyncSingletonProducerDependentBean beanInstance) {
                assertNotNull(beanInstance);

                assertNotNull(beanInstance.getLaBean());
                assertNotNull(beanInstance.getLaBean2());

                assertSame(beanInstance.getLaBean(), beanInstance.getLaBean2());

                finishTest();
              }
            });
      }
    }.schedule(100);

  }

  public void testBeanConstrConsumesOwnProduer() {
    delayTestFinish(10000);

      new Timer() {
        @Override
        public void run() {
          IOC.getAsyncBeanManager().lookupBean(BeanConstrConsumesOwnProducer.class)
              .getInstance(new CreationalCallback<BeanConstrConsumesOwnProducer>() {
                @Override
                public void callback(final BeanConstrConsumesOwnProducer beanInstance) {
                  assertNotNull(beanInstance);
                  assertNotNull(beanInstance.getWrappedKitten());
                  assertNotNull(beanInstance.getWrappedKitten().getKitten());

                  finishTest();
                }
              });
        }
      }.schedule(100);

  }
}