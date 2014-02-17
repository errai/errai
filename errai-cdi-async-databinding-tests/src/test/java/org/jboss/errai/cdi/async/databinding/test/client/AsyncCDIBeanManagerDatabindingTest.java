package org.jboss.errai.cdi.async.databinding.test.client;

import org.jboss.errai.cdi.async.databinding.test.client.res.MyBean;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;

/**
 * @author Mike Brock
 */
public class AsyncCDIBeanManagerDatabindingTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.async.databinding.test.AsyncCDIDatabindingeTest";
  }

  public void testModelInjectionWorksWithAsyncBeanManager() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanManager bm = IOC.getAsyncBeanManager();
        bm.lookupBean(MyBean.class)
            .getInstance(new CreationalCallback<MyBean>() {
              @Override
              public void callback(MyBean beanInstance) {
                assertNotNull(beanInstance.getModel());
                beanInstance.getModel().setName("foo");
                assertEquals("foo", beanInstance.getName().getValue());
                finishTest();
              }
            });
      }
    });
  }
}
