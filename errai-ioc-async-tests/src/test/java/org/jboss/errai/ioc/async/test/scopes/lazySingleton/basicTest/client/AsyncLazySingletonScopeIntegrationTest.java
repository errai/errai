package org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res.DependendBeanWithSingleton;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res.LazySingletonBean;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res.SingletonBean;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res.SomeLonelEySingleton;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.user.client.Timer;

/**
 * @author mariusgerwinn
 */
public class AsyncLazySingletonScopeIntegrationTest extends IOCClientTestCase {

  private static Object singletonInstance;
  private static Object lazySingletonInstance;

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.AsyncLazySingletonScopeBasicTests";
  }

  @Override
  protected void gwtTearDown() throws Exception {
    super.gwtTearDown();
    org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res.LazySingletonBean.instances = 0;
    SingletonBean.instances = 0;
  }
  
  public void testLazySingletonPostConstructWithBeanManager() {
    delayTestFinish(10000);
    Container.runAfterInit(new Runnable() {
        
        @Override
        public void run() {
            IOC.getAsyncBeanManager().lookupBean(LazySingletonBean.class).getInstance(new CreationalCallback<LazySingletonBean>() {
                
                @Override
                public void callback(LazySingletonBean beanInstance) {
                    beanInstance.doSomeTHing();
                    finishTest();
                }
            });
        }
    });
}
  private static int called;
  private static int called2;
  public void testRunAfteriNitWorks() {
    delayTestFinish(10000);
    Container.runAfterInit(new Runnable() {
      public void run() {
        assertTrue(called==0);
        called++;
        // waint since run after init usaly fails async a few miliseconds after
        // runnable has been called
        new Timer() {
          @Override
          public void run() {
            finishTest();

          }
        }.schedule(1000);
      };
    });
  }
  public void testRunAfteriNitWorks2() {
    delayTestFinish(10000);
    Container.runAfterInit(new Runnable() {
      public void run() {
        assertTrue(called2==0);
        called2++;
        // waint since run after init usaly fails async a few miliseconds after
        // runnable has been called
        new Timer() {
          @Override
          public void run() {
            finishTest();
            
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
        
        new Timer() {

          @Override
          public void run() {
            assertSame(
                    "Lazy Singleton is already created by the container",
                    0,
                    org.jboss.errai.ioc.async.test.scopes.lazySingleton.basicTest.client.res.LazySingletonBean.instances);
            assertSame("Singleton is not created", 1, SingletonBean.instances);

            finishTest();

          }
        }.schedule(1000);
      }
    });
  }

  private void singletonSetup() {
    Container.runAfterInit(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(SomeLonelEySingleton.class)
                .getInstance(new CreationalCallback<SomeLonelEySingleton>() {

                  @Override
                  public void callback(SomeLonelEySingleton beanInstance) {
                    // since order in tests is not guranteed we need to run this
                    // test twice. first time it will be null
                    if (singletonInstance == null)
                      singletonInstance = beanInstance;
                    else
                      assertNotSame(singletonInstance, beanInstance);
                    finishTest();
                  }
                });
      }
    });
  }

  public void testSingletonDiffersInEachSetup() {
    delayTestFinish(60000);
    // since order in tests is not guranteed we need to run this test twice.
    // first time it will be null
    singletonSetup();
  }

  public void testSingletonDiffersInEachSetup2() {
    delayTestFinish(60000);
    // since order in tests is not guranteed we need to run this test twice.
    // first time it will be null
    singletonSetup();
  }
  
  
  private void lazySingletonSetup() {
    Container.runAfterInit(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(LazySingletonBean.class)
                .getInstance(new CreationalCallback<LazySingletonBean>() {

                  @Override
                  public void callback(LazySingletonBean beanInstance) {
                    // since order in tests is not guranteed we need to run this
                    // test twice. first time it will be null
                    if (lazySingletonInstance == null)
                      lazySingletonInstance = beanInstance;
                    else
                      assertNotSame(lazySingletonInstance, beanInstance);
                    
                    beanInstance.doSomeTHing();
                    finishTest();
                  }
                });
      }
    });
  }

  public void testLazySingletonDiffersInEachSetup() {
    delayTestFinish(60000);
    // since order in tests is not guranteed we need to run this test twice.
    // first time it will be null
    lazySingletonSetup();
  }

  public void testLazySingletonDiffersInEachSetup2() {
    delayTestFinish(60000);
    // since order in tests is not guranteed we need to run this test twice.
    // first time it will be null
    lazySingletonSetup();
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
                          private LazySingletonBean singletonBeanInjected;

                          @Override
                          public void callback(
                                  final DependendBeanWithSingleton provider) {
                            assertNotNull(provider.getBean());
                            singletonBeanInjected = provider.getBean();
                            singletonBeanInjected.doSomeTHing();

                            IOC.getAsyncBeanManager()
                                    .lookupBean(LazySingletonBean.class)
                                    .getInstance(
                                            new CreationalCallback<LazySingletonBean>() {
                                              @Override
                                              public void callback(
                                                      final LazySingletonBean bean) {
                                                assertNotNull(bean);
                                                assertSame(
                                                        "A values are not the same instances",
                                                        singletonBeanInjected,
                                                        bean);
                                                bean.doSomeTHing();
                                                finishTest();
                                              }
                                            });
                          }
                        });
      }
    });
  }

}