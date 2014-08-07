package org.jboss.errai.ioc.async.test.beanmanager.client;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.ADependent;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.AirDependentBean;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Bar;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Cow;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Foo;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.Pig;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.SomeOtherOtherSingleton;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.SomeOtherSingleton;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.SomeSingleton;
import org.jboss.errai.ioc.async.test.beanmanager.client.res.TestInterface;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;

import com.google.gwt.user.client.Timer;

import java.util.Collection;

/**
 * @author Mike Brock
 */
public class AsyncBeanManagerTests extends IOCClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.beanmanager.AsyncBeanManagerTests";
  }
  
  @Override
  protected void gwtTearDown() throws Exception {
    super.gwtTearDown();
    SomeSingleton.instances=0;
    SomeOtherSingleton.instances=0;
    SomeOtherOtherSingleton.instances=0;
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
  

  public void testComplexSingletonWithBeanManagerSzenario() {
    delayTestFinish(25000);

    Container.$(new Runnable() {
      @Override
      public void run() {
            final AsyncBeanManager asyncBeanManager = IOC.getAsyncBeanManager();
            // we wait a bit more than after init to ensure all instances are created
            new Timer() {
                
                @Override
                public void run() {
                    // we except instances upfront, since some test may run before this test. we don't know how much instacnes we have
                    // anyhow getting bean will create a new instance
                    assertEquals(1, SomeSingleton.instances);
                    assertEquals(1, SomeOtherSingleton.instances);
                    assertEquals(1, SomeOtherOtherSingleton.instances);
                    
                    // wait until someothersingleton has loaded everything in its postconsctruct call
                    new Timer() {
                        
                        @Override
                        public void run() {
                            // decoupled thread is loading some instances with bean manager async. wait until done
                            assertEquals(1, SomeSingleton.instances);
                            assertEquals(1, SomeOtherSingleton.instances);
                            assertEquals(1, SomeOtherOtherSingleton.instances);
                            
                            asyncBeanManager.lookupBean(SomeSingleton.class).getInstance(new CreationalCallback<SomeSingleton>() {
                                
                                @Override
                                public void callback(SomeSingleton beanInstance) {
                                    assertEquals(1, SomeSingleton.instances);
                                    // we wait to ensure that the postcontruct method has bean called yet & is fully processed
                                    // event for async calls like calling the bean manager
                                    new Timer() {
                                        
                                        @Override
                                        public void run() {
                                            // no the nested bean manager cal should have created this intances
                                            assertEquals(1, SomeOtherSingleton.instances);
                                            assertEquals(1, SomeOtherOtherSingleton.instances);
                                            
                                            // try to do something simlar
                                            asyncBeanManager.lookupBean(SomeSingleton.class).getInstance(new CreationalCallback<SomeSingleton>() {
                                                
                                                @Override
                                                public void callback(SomeSingleton beanInstance) {
                                                    assertEquals(1, SomeOtherOtherSingleton.instances);
                                                    finishTest();
                                                }
                                                
                                            });
                                        }
                                    }.schedule(1000);
                                }
                            });
                            
                        }
                    }.schedule(1000);
                    
                }
            }.schedule(1000);
        }
    });
  }
}

