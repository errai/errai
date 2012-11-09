package org.jboss.errai.ioc.async.test.constructor.client;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.ioc.async.test.constructor.client.res.ConstrInjBean;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCEnvironment;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;

/**
 * @author Mike Brock
 */
public class AsyncConstructorInjectionTests extends IOCClientTestCase {
  @Override
  public void gwtSetUp() throws Exception {
    IOCEnvironment.setAsync(true);
    super.gwtSetUp();
  }


  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.constructor.AsyncConstrInjectTests";
  }

  public void testBeanConstructedViaConstructor() {
    delayTestFinish(10000);
    new Timer() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(ConstrInjBean.class)
            .getInstance(new CreationalCallback<ConstrInjBean>() {
              @Override
              public void callback(final ConstrInjBean beanInstance) {

                assertNotNull(beanInstance.getMyself());
                assertNotNull(beanInstance.getApple());
                assertNotNull(beanInstance.getPear());
                assertNotNull(beanInstance.getOrange());

                assertTrue(beanInstance.isPostConstructFired());

                assertSame(beanInstance, IOC.getAsyncBeanManager().getActualBeanReference(beanInstance.getMyself()));

                finishTest();
              }
            });
      }
    }.schedule(100);
  }
}
