package org.jboss.errai.ioc.async.test.beanmanager.client;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.ADependent;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.AirDependentBean;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Bar;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Cow;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Foo;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Pig;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.TestInterface;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public class AsyncBeanManagerTests extends IOCClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.beanmanager.AsyncBeanManagerTests";
  }

  public void testAsyncLookup() {
    delayTestFinish(10000);

    Container.$(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(Foo.class).getInstance(new CreationalCallback<Foo>() {
          @Override
          public void callback(final Foo bean) {
            assertNotNull(bean);
            assertNotNull(bean.getBar());
            assertNotNull(bean.getBar2());
            assertNotNull(bean.getBarDisposer());
            assertNotNull(bean.getBar2().getManager());
            assertNotNull(bean.getBazTheSingleton());
            assertNotNull(bean.getBar().getBazTheSingleton());
            assertNotNull(bean.getBar2().getBazTheSingleton());

            assertSame(bean.getBazTheSingleton(), bean.getBar().getBazTheSingleton());
            assertSame(bean.getBazTheSingleton(), bean.getBar2().getBazTheSingleton());

            final Object fooRef1 = IOC.getAsyncBeanManager().getActualBeanReference(bean.getBar().getFoo());
            final Object fooRef2 = IOC.getAsyncBeanManager().getActualBeanReference(bean);

            assertSame(fooRef1, fooRef2);

            // confirm post-construct fired
            assertTrue(bean.getBar().isPostContr());

            System.out.println("foo.bar=" + bean.getBar());
            finishTest();
          }
        });
      }
    });
  }

  public void testCreateAndDestroyBean() {
    delayTestFinish(10000);

    Container.$(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(Bar.class).getInstance(new CreationalCallback<Bar>() {
          @Override
          public void callback(final Bar bean) {
            assertTrue(IOC.getAsyncBeanManager().isManaged(bean));

            IOC.getAsyncBeanManager().destroyBean(bean);

            assertFalse(IOC.getAsyncBeanManager().isManaged(bean));

            finishTest();
          }
        });
      }
    });
  }

  public void testLookupDependentBean() {
    delayTestFinish(10000);

    Container.$(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(ADependent.class)
            .getInstance(new CreationalCallback<ADependent>() {
              @Override
              public void callback(final ADependent bean) {
                assertNotNull(bean);

                assertEquals("foo", bean.testString());

                finishTest();
              }
            });
      }
    });
  }

  public void testLookupFromSuperTypes() {
    delayTestFinish(10000);

    Container.$(new Runnable() {
      @Override
      public void run() {
        final Collection<AsyncBeanDef<TestInterface>> asyncBeanDefs = IOC.getAsyncBeanManager().lookupBeans(TestInterface.class);

        assertEquals(2, asyncBeanDefs.size());

        finishTest();
      }
    });
  }

  public void testNamedLookupsOfBean() {
    delayTestFinish(10000);

    Container.$(new Runnable() {
      @Override
      public void run() {
        final Collection<AsyncBeanDef> beans = IOC.getAsyncBeanManager().lookupBeans("animal");

        assertEquals("wrong number of beans", 2, beans.size());
        assertTrue("should contain a pig", containsInstanceOf(beans, Pig.class));
        assertTrue("should contain a cow", containsInstanceOf(beans, Cow.class));

        finishTest();
      }
    });
  }

  public void testBeanInjectedByInterface() {
    delayTestFinish(100000);

    Container.$(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(AirDependentBean.class)
            .getInstance(new CreationalCallback<AirDependentBean>() {
              @Override
              public void callback(AirDependentBean beanInstance) {
                assertNotNull(beanInstance);
                assertNotNull(beanInstance.getAir());
                finishTest();
              }
            });
      }
    });
  }


  private static boolean containsInstanceOf(final Collection<AsyncBeanDef> defs, final Class<?> clazz) {
    for (final AsyncBeanDef def : defs) {
      if (def.getType().equals(clazz)) return true;
    }
    return false;
  }
}

