package org.jboss.errai.ioc.async.test.scopes.lazySingleton.client;

import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.ioc.async.test.scopes.dependent.client.res.ApplicationScopedBean;
import org.jboss.errai.ioc.async.test.scopes.dependent.client.res.DependentScopedBean;
import org.jboss.errai.ioc.async.test.scopes.dependent.client.res.DependentScopedBeanWithDependencies;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.DependenBeanWithProvidedBean;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.DependendBean2WithSingleton;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.DependendBeanWithSingleton;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.LazySingletonTestUtil;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.MyLazyBeanInterface;
import org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.ProvidedBean;
import org.jboss.errai.ioc.client.Container;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;

import com.google.gwt.user.client.Timer;

/**
 * @author mariusgerwinn
 */
public class AsyncLazySingletonScopeIntegrationTest extends IOCClientTestCase {

	@Override
	public String getModuleName() {
		return "org.jboss.errai.ioc.async.test.scopes.lazySingleton.AsyncLazySingletonScopeTests";
	}

	public void testLazySingletonNotCreatedByContainer() {
		delayTestFinish(10000);
		assertFalse(
				"Lazy Singleton is already created by the container",
				LazySingletonTestUtil
						.getOrderOfCreation()
						.contains(
								"org.jboss.errai.ioc.async.test.scopes.lazySingleton.client.res.LazySingletonBean"));
		finishTest();
	}

	static DependendBeanWithSingleton dependendBean1;

	static DependendBeanWithSingleton dependendBean2;

	static DependendBean2WithSingleton dependendBean21;

	static DependendBean2WithSingleton dependendBean22;

	public void testSameInstanceOfLazyNonLazySingletonRetrievedDueToInjection() {
		delayTestFinish(10000);

		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
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
											}
										});
					}
				}.schedule(1500);
			}
		});

		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
					@Override
					public void run() {
						IOC.getAsyncBeanManager()
								.lookupBean(DependendBeanWithSingleton.class)
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
											}
										});
					}
				}.schedule(1500);
			}
		});

		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
					@Override
					public void run() {
						IOC.getAsyncBeanManager()
								.lookupBean(DependendBean2WithSingleton.class)
								.getInstance(
										new CreationalCallback<DependendBean2WithSingleton>() {
											@Override
											public void callback(
													final DependendBean2WithSingleton provider) {
												dependendBean21 = provider;
											}
										});
					}
				}.schedule(1500);
			}
		});

		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
					@Override
					public void run() {
						IOC.getAsyncBeanManager()
								.lookupBean(DependendBean2WithSingleton.class)
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
				}.schedule(1500);
			}
		});
	}

//	public void testProducerLazy() {
//		delayTestFinish(10000);
//		assertFalse(
//				"Lazy singlton produced bean is already there",
//				LazySingletonTestUtil.getOrderOfCreation().contains(
//						"com.fileee.core.client.iocTest.ProducedBean"));
//		assertFalse(
//				"Lazy singlton produced bean Provider is already there",
//				LazySingletonTestUtil.getOrderOfCreation().contains(
//						"com.fileee.core.client.iocTest.MyProducer"));
//
//		Container.runAfterInit(new Runnable() {
//			@Override
//			public void run() {
//				new Timer() {
//					@Override
//					public void run() {
//						IOC.getAsyncBeanManager()
//								.lookupBean(ProducedBeanContainer.class)
//								.getInstance(
//										new CreationalCallback<ProducedBeanContainer>() {
//											@Override
//											public void callback(
//													final ProducedBeanContainer provider) {
//												assertNotNull(
//														"ProducedBean is null",
//														provider.getBean());
//												assertTrue(
//														"Produced bean should be there",
//														LazySingletonTestUtil
//																.getOrderOfCreation()
//																.contains(
//																		"com.fileee.core.client.iocTest.ProducedBean"));
//												finishTest();
//											}
//										});
//					}
//				}.schedule(1500);
//			}
//		});
//	}

	static MyLazyBeanInterface myLazySingletonBean1;

	static MyLazyBeanInterface myLazySingletonBean2;

	public void testGettingBeanWithManager() {
		delayTestFinish(10000);

		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
					@Override
					public void run() {
						IOC.getAsyncBeanManager()
								.lookupBean(MyLazyBeanInterface.class)
								.getInstance(
										new CreationalCallback<MyLazyBeanInterface>() {
											@Override
											public void callback(
													final MyLazyBeanInterface provider) {
												myLazySingletonBean1 = provider;
												assertNotNull(provider);
											}
										});
					}
				}.schedule(1500);
			}
		});

		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
					@Override
					public void run() {
						IOC.getAsyncBeanManager()
								.lookupBean(MyLazyBeanInterface.class)
								.getInstance(
										new CreationalCallback<MyLazyBeanInterface>() {
											@Override
											public void callback(
													final MyLazyBeanInterface provider) {
												myLazySingletonBean2 = provider;
												assertNotNull(provider);
												assertSame(
														"A values are not the same instances",
														myLazySingletonBean1,
														myLazySingletonBean2);
												finishTest();
											}
										});
					}
				}.schedule(1500);
			}
		});
	}

	static ProvidedBean providedBean1;

	static ProvidedBean providedBean2;

	public void testProvidedBean() {
		delayTestFinish(10000);

		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
					@Override
					public void run() {
						IOC.getAsyncBeanManager()
								.lookupBean(DependenBeanWithProvidedBean.class)
								.getInstance(
										new CreationalCallback<DependenBeanWithProvidedBean>() {
											@Override
											public void callback(
													final DependenBeanWithProvidedBean provider) {
												assertNotNull(provider
														.getBean());
												finishTest();
											}
										});
					}
				}.schedule(1500);
			}
		});
	}

	private static MyLazyBeanInterface singletonBeanInjected;

	private static MyLazyBeanInterface singletonBeanRetrievedByBeanManager;

	public void testBeanManagerAndNormalInjectedIsTheSameInstance() {
		delayTestFinish(10000);

		
		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
					@Override
					public void run() {
						IOC.getAsyncBeanManager()
								.lookupBean(DependendBeanWithSingleton.class)
								.getInstance(
										new CreationalCallback<DependendBeanWithSingleton>() {
											@Override
											public void callback(
													final DependendBeanWithSingleton provider) {
												assertNotNull(provider.getLazySingletonBean());
												singletonBeanInjected = provider.getLazySingletonBean();
											}
										});
					}
				}.schedule(1500);
			}
		});
		
		
		Container.runAfterInit(new Runnable() {
			@Override
			public void run() {
				new Timer() {
					@Override
					public void run() {
						IOC.getAsyncBeanManager()
								.lookupBean(MyLazyBeanInterface.class)
								.getInstance(
										new CreationalCallback<MyLazyBeanInterface>() {
											@Override
											public void callback(
													final MyLazyBeanInterface provider) {
												assertNotNull(provider);
												singletonBeanRetrievedByBeanManager = provider;
												assertSame("A values are not the same instances",
														singletonBeanInjected,
														singletonBeanRetrievedByBeanManager);
												finishTest();
											}
										});
					}
				}.schedule(1500);
			}
		});
	}

}