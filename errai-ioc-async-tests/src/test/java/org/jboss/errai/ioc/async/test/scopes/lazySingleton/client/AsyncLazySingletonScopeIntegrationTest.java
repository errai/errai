package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.DependenBeanWithProvidedBean;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.DependendBean2WithSingleton;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.DependendBeanWithSingleton;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.LazySingletonTestUtil;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.MyLazyBeanInterface;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.ProvidedBean;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.SomeDependendBeanThatLoadsLazySIngletobBeanWithBeanManager;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.SomeLazySingletonBeanForBeanManager;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.SomeSingletonBean;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.SomeSingletonBeanThatLoadsLazySIngletobBeanWithBeanManager;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.user.client.Timer;

public class AsyncLazySingletonScopeIntegrationTest extends IOCClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.scopes.lazySingleton.AsyncLazySingletonScopeTests";
  }

  @Override
  protected void gwtTearDown() throws Exception {
    SomeSingletonBeanThatLoadsLazySIngletobBeanWithBeanManager.instances = 0;
    SomeSingletonBean.instances = 0;
    SomeLazySingletonBeanForBeanManager.instances = 0;
    super.gwtTearDown();
  }

  public void testRunAfteriNitWorks() {
    delayTestFinish(10000);
    Container.runAfterInit(new Runnable() {
      public void run() {
        // waint since run after init usaly fails async a few miliseconds after
        // runnable has been called
        new Timer() {
          @Override
          public void run() {
            IOC.getAsyncBeanManager()
                    .lookupBean(SomeLazySingletonBeanForBeanManager.class)
                    .getInstance(
                            new CreationalCallback<SomeLazySingletonBeanForBeanManager>() {

                              @Override
                              public void callback(
                                      SomeLazySingletonBeanForBeanManager beanInstance) {
                                new Timer() {
                                  @Override
                                  public void run() {
                                    finishTest();
                                  }
                                }.schedule(1000);
                              }
                            });

          }
        }.schedule(1000);
      };
    });
  }

  public void testLazySingletonNotCreatedByContainer() {
    delayTestFinish(10000);
    Container.runAfterInit(new Runnable() {
      @Override
      public void run() {
        assertFalse(
                "Lazy Singleton is already created by the container",
                LazySingletonTestUtil
                        .getOrderOfCreation()
                        .contains(

                                "org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.LazySingletonBean"));
        finishTest();
      }
    });
  }

  public void testSameInstanceOfLazyNonLazySingletonRetrievedDueToInjection() {
    delayTestFinish(15000);

    Container.runAfterInit(new Runnable() {
      DependendBeanWithSingleton dependendBean1;

      DependendBeanWithSingleton dependendBean2;

      DependendBean2WithSingleton dependendBean21;

      DependendBean2WithSingleton dependendBean22;

      @Override
      public void run() {
        IOC.getAsyncBeanManager()
                .lookupBean(DependendBeanWithSingleton.class)
                .getInstance(
                        new CreationalCallback<DependendBeanWithSingleton>() {
                          @Override
                          public void callback(
                                  final DependendBeanWithSingleton dependendBeanWithSingleton) {
                            dependendBean1 = dependendBeanWithSingleton;

                            IOC.getAsyncBeanManager()
                                    .lookupBean(
                                            DependendBeanWithSingleton.class)
                                    .getInstance(
                                            new CreationalCallback<DependendBeanWithSingleton>() {
                                              @Override
                                              public void callback(
                                                      final DependendBeanWithSingleton provider) {
                                                dependendBean2 = provider;
                                                assertNotSame(
                                                        "DependendBeanWithSingleton are the same",
                                                        dependendBean1,
                                                        dependendBean2);
                                                assertSame(
                                                        "A values are not the same instances",
                                                        dependendBean1
                                                                .getLazySingletonBean(),
                                                        dependendBean2
                                                                .getLazySingletonBean());
                                                assertSame(
                                                        "Singleton retrieved instances are not the same ",
                                                        dependendBean1
                                                                .getBean2(),
                                                        dependendBean2
                                                                .getBean2());

                                                IOC.getAsyncBeanManager()
                                                        .lookupBean(
                                                                DependendBean2WithSingleton.class)
                                                        .getInstance(
                                                                new CreationalCallback<DependendBean2WithSingleton>() {
                                                                  @Override
                                                                  public void callback(
                                                                          final DependendBean2WithSingleton provider) {
                                                                    dependendBean21 = provider;

                                                                    IOC.getAsyncBeanManager()
                                                                            .lookupBean(
                                                                                    DependendBean2WithSingleton.class)
                                                                            .getInstance(
                                                                                    new CreationalCallback<DependendBean2WithSingleton>() {
                                                                                      @Override
                                                                                      public void callback(
                                                                                              final DependendBean2WithSingleton provider) {
                                                                                        dependendBean22 = provider;
                                                                                        assertNotSame(
                                                                                                "DependendBeanWithSingleton are the same",
                                                                                                dependendBean21,
                                                                                                dependendBean22);
                                                                                        assertSame(
                                                                                                "Values are not the same instances",
                                                                                                dependendBean21
                                                                                                        .getLazySingletonBean(),
                                                                                                dependendBean22
                                                                                                        .getLazySingletonBean());
                                                                                        assertSame(
                                                                                                "Values are not the same instances",
                                                                                                dependendBean1
                                                                                                        .getLazySingletonBean(),
                                                                                                dependendBean21
                                                                                                        .getLazySingletonBean());
                                                                                        finishTest();
                                                                                      }
                                                                                    });
                                                                  }
                                                                });
                                              }
                                            });
                          }
                        });

      }
    });

  }

  public void testGettingBeanWithManager() {
    delayTestFinish(10000);

    Container.runAfterInit(new Runnable() {
      MyLazyBeanInterface myLazySingletonBean1;

      MyLazyBeanInterface myLazySingletonBean2;

      @Override
      public void run() {
        new Timer() {
          @Override
          public void run() {
            IOC.getAsyncBeanManager().lookupBean(MyLazyBeanInterface.class)
                    .getInstance(new CreationalCallback<MyLazyBeanInterface>() {
                      @Override
                      public void callback(final MyLazyBeanInterface provider) {
                        myLazySingletonBean1 = provider;
                        assertNotNull(provider);
                      }
                    });
          }
        }.schedule(1500);

        new Timer() {
          @Override
          public void run() {
            IOC.getAsyncBeanManager().lookupBean(MyLazyBeanInterface.class)
                    .getInstance(new CreationalCallback<MyLazyBeanInterface>() {
                      @Override
                      public void callback(final MyLazyBeanInterface provider) {
                        myLazySingletonBean2 = provider;
                        assertNotNull(provider);
                        assertSame("A values are not the same instances",
                                myLazySingletonBean1, myLazySingletonBean2);
                        finishTest();
                      }
                    });
          }
        }.schedule(1500);
      }
    });

  }

  public void testProvidedBean() {
    delayTestFinish(10000);

    Container.runAfterInit(new Runnable() {
      ProvidedBean providedBean1;

      ProvidedBean providedBean2;

      @Override
      public void run() {
        IOC.getAsyncBeanManager()
                .lookupBean(DependenBeanWithProvidedBean.class)
                .getInstance(
                        new CreationalCallback<DependenBeanWithProvidedBean>() {
                          @Override
                          public void callback(
                                  final DependenBeanWithProvidedBean provider) {
                            assertNotNull(provider.getBean());
                            finishTest();
                          }
                        });
      }
    });
  }

  public void testPostConstructOrder() {
    delayTestFinish(20000);
    Container.$(new Runnable() {

      @Override
      public void run() {
        assertSame(1, SomeSingletonBean.instances);
        assertSame(
                1,
                SomeSingletonBeanThatLoadsLazySIngletobBeanWithBeanManager.instances);
        finishTest();
      }
    });
  }

  
  public void testGettingDependedBeanWithLazySingletonBean() {
    delayTestFinish(20000);
    Container.$(new Runnable() {

      @Override
      public void run() {
       IOC.getAsyncBeanManager().lookupBean(SomeDependendBeanThatLoadsLazySIngletobBeanWithBeanManager.class).getInstance(new CreationalCallback<SomeDependendBeanThatLoadsLazySIngletobBeanWithBeanManager>() {
        
        @Override
        public void callback(
                SomeDependendBeanThatLoadsLazySIngletobBeanWithBeanManager beanInstance) {
          new Timer() {
            
            @Override
            public void run() {
              finishTest();
            }
          }.schedule(500);
        }
      });
      }
    });
  }
  public void testBeanManagerAndNormalInjectedIsTheSameInstance() {
    delayTestFinish(60000);

    Container.runAfterInit(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager()
                .lookupBean(DependendBeanWithSingleton.class)
                .getInstance(
                        new CreationalCallback<DependendBeanWithSingleton>() {
                          private MyLazyBeanInterface singletonBeanInjected;

                          private MyLazyBeanInterface singletonBeanRetrievedByBeanManager;

                          @Override
                          public void callback(
                                  final DependendBeanWithSingleton provider) {
                            assertNotNull(provider.getLazySingletonBean());
                            singletonBeanInjected = provider
                                    .getLazySingletonBean();

                            IOC.getAsyncBeanManager()
                                    .lookupBean(MyLazyBeanInterface.class)
                                    .getInstance(
                                            new CreationalCallback<MyLazyBeanInterface>() {
                                              @Override
                                              public void callback(
                                                      final MyLazyBeanInterface provider) {
                                                assertNotNull(provider);
                                                singletonBeanRetrievedByBeanManager = provider;
                                                assertSame(
                                                        "A values are not the same instances",
                                                        singletonBeanInjected,
                                                        singletonBeanRetrievedByBeanManager);
                                                finishTest();
                                              }
                                            });
                          }
                        });
      }
    });
  }
}