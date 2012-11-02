package org.jboss.errai.ioc.async.test.client;

import com.google.gwt.user.client.Timer;
import org.jboss.errai.ioc.async.test.client.res.Foo;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCEnvironment;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;

import java.sql.Time;

/**
 * @author Mike Brock
 */
public class IOCAsyncTests extends IOCClientTestCase {
  @Override
  public void gwtSetUp() throws Exception {
    IOCEnvironment.setAsync(true);
    super.gwtSetUp();
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.AsyncTests";
  }

  public void testAsyncLookup() {
    new Timer() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(Foo.class).getInstance(new CreationalCallback<Foo>() {
          @Override
          public void callback(final Foo beanInstance) {
            assertNotNull(beanInstance);
            assertNotNull(beanInstance.getBar());
            assertNotNull(beanInstance.getBar2());
            assertNotNull(beanInstance.getBarDisposer());

            assertNotNull(beanInstance.getBar2().getManager());

            System.out.println("foo.bar=" + beanInstance.getBar());
            finishTest();
          }
        });
      }
    }.schedule(3000);

    delayTestFinish(10000);
  }
}
