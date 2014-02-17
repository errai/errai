package org.jboss.errai.ioc.async.test.constructor.client;

import com.google.gwt.user.client.Timer;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.async.test.constructor.client.res.ConstrInjBean;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * @author Mike Brock
 */
public class AsyncConstructorInjectionTests extends IOCClientTestCase {

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.constructor.AsyncConstrInjectTests";
  }

  public void testBeanConstructedViaConstructor() {
    delayTestFinish(10000);
    Container.runAfterInit(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(ConstrInjBean.class)
            .getInstance(new CreationalCallback<ConstrInjBean>() {
              @Override
              public void callback(final ConstrInjBean bean) {

                assertNotNull(bean.getMyself());
                assertNotNull(bean.getApple());
                assertNotNull(bean.getPear());
                assertNotNull(bean.getOrange());

                assertNotNull(bean.getPeanut());
                assertNotNull(bean.getCashew());

                assertTrue(bean.isPostConstructFired());

                assertSame(bean, IOC.getAsyncBeanManager().getActualBeanReference(bean.getMyself()));

                finishTest();
              }
            });
      }
    });
  }
}
