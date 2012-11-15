package org.jboss.errai.ioc.async.test.beanmanager.client;

import org.jboss.errai.ioc.async.test.beanmanager.client.res.ADependent;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Bar;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Foo;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCEnvironment;
import org.jboss.errai.ioc.client.container.async.CreationalCallback;

/**
 * @author Mike Brock
 */
public class AsyncBeanManagerTests extends IOCClientTestCase {
  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.beanmanager.AsyncBeanManagerTests";
  }

  public void testAsyncLookup() {
    delayTestFinish(10000);

    IOC.getAsyncBeanManager().lookupBean(Foo.class).getInstance(new CreationalCallback<Foo>() {
      @Override
      public void callback(final Foo beanInstance) {
        assertNotNull(beanInstance);
        assertNotNull(beanInstance.getBar());
        assertNotNull(beanInstance.getBar2());
        assertNotNull(beanInstance.getBarDisposer());
        assertNotNull(beanInstance.getBar2().getManager());
        assertNotNull(beanInstance.getBazTheSingleton());
        assertNotNull(beanInstance.getBar().getBazTheSingleton());
        assertNotNull(beanInstance.getBar2().getBazTheSingleton());

        assertSame(beanInstance.getBazTheSingleton(), beanInstance.getBar().getBazTheSingleton());
        assertSame(beanInstance.getBazTheSingleton(), beanInstance.getBar2().getBazTheSingleton());

        final Object fooRef1 = IOC.getAsyncBeanManager().getActualBeanReference(beanInstance.getBar().getFoo());
        final Object fooRef2 = IOC.getAsyncBeanManager().getActualBeanReference(beanInstance);

        assertSame(fooRef1, fooRef2);

        // confirm post-construct fired
        assertTrue(beanInstance.getBar().isPostContr());

        System.out.println("foo.bar=" + beanInstance.getBar());
        finishTest();
      }
    });
  }

  public void testCreateAndDestroyBean() {
    delayTestFinish(10000);
    IOC.getAsyncBeanManager().lookupBean(Bar.class).getInstance(new CreationalCallback<Bar>() {
      @Override
      public void callback(final Bar beanInstance) {
        assertTrue(IOC.getAsyncBeanManager().isManaged(beanInstance));

        IOC.getAsyncBeanManager().destroyBean(beanInstance);

        assertFalse(IOC.getAsyncBeanManager().isManaged(beanInstance));

        finishTest();
      }
    });
  }

  public void testLookupDependentBean() {
    delayTestFinish(10000);
    IOC.getAsyncBeanManager().lookupBean(ADependent.class)
        .getInstance(new CreationalCallback<ADependent>() {
          @Override
          public void callback(final ADependent beanInstance) {
            assertNotNull(beanInstance);

            assertEquals("foo", beanInstance.testString());

            finishTest();
          }
        });
  }
}
