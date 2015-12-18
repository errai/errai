/*
 * Copyright (C) 2012 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.async.test.bm.client;

import java.lang.annotation.Annotation;
import java.util.Collection;
import java.util.Set;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.inject.Any;

import org.jboss.errai.cdi.async.test.bm.client.res.AbstractBean;
import org.jboss.errai.cdi.async.test.bm.client.res.ActivatedBean;
import org.jboss.errai.cdi.async.test.bm.client.res.ActivatedBeanInterface;
import org.jboss.errai.cdi.async.test.bm.client.res.ApplicationScopedBean;
import org.jboss.errai.cdi.async.test.bm.client.res.CommonInterface;
import org.jboss.errai.cdi.async.test.bm.client.res.CommonInterfaceB;
import org.jboss.errai.cdi.async.test.bm.client.res.Cow;
import org.jboss.errai.cdi.async.test.bm.client.res.CreditCard;
import org.jboss.errai.cdi.async.test.bm.client.res.DependentScopedBean;
import org.jboss.errai.cdi.async.test.bm.client.res.DependentScopedBeanWithDependencies;
import org.jboss.errai.cdi.async.test.bm.client.res.FoobieScopedBean;
import org.jboss.errai.cdi.async.test.bm.client.res.FoobieScopedOverriddenBean;
import org.jboss.errai.cdi.async.test.bm.client.res.InheritedApplicationScopedBean;
import org.jboss.errai.cdi.async.test.bm.client.res.InheritedFromAbstractBean;
import org.jboss.errai.cdi.async.test.bm.client.res.InterfaceA;
import org.jboss.errai.cdi.async.test.bm.client.res.InterfaceB;
import org.jboss.errai.cdi.async.test.bm.client.res.InterfaceC;
import org.jboss.errai.cdi.async.test.bm.client.res.InterfaceD;
import org.jboss.errai.cdi.async.test.bm.client.res.InterfaceRoot;
import org.jboss.errai.cdi.async.test.bm.client.res.LincolnBar;
import org.jboss.errai.cdi.async.test.bm.client.res.OuterBeanInterface;
import org.jboss.errai.cdi.async.test.bm.client.res.Pig;
import org.jboss.errai.cdi.async.test.bm.client.res.QualA;
import org.jboss.errai.cdi.async.test.bm.client.res.QualAppScopeBeanA;
import org.jboss.errai.cdi.async.test.bm.client.res.QualAppScopeBeanB;
import org.jboss.errai.cdi.async.test.bm.client.res.QualB;
import org.jboss.errai.cdi.async.test.bm.client.res.QualEnum;
import org.jboss.errai.cdi.async.test.bm.client.res.QualParmAppScopeBeanApples;
import org.jboss.errai.cdi.async.test.bm.client.res.QualParmAppScopeBeanOranges;
import org.jboss.errai.cdi.async.test.bm.client.res.QualV;
import org.jboss.errai.cdi.async.test.bm.client.res.TestBeanActivator;
import org.jboss.errai.cdi.async.test.bm.client.res.ViaInstanceModule;
import org.jboss.errai.cdi.async.test.bm.client.res.Visa;
import org.jboss.errai.common.client.util.CreationalCallback;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.QualifierUtil;
import org.jboss.errai.ioc.client.container.DestructionCallback;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.container.RefHolder;
import org.jboss.errai.ioc.client.container.async.AsyncBeanDef;
import org.jboss.errai.ioc.client.container.async.AsyncBeanFuture;
import org.jboss.errai.ioc.client.container.async.AsyncBeanManager;
import org.jboss.errai.ioc.client.container.async.AsyncBeanQuery;

/**
 * @author Mike Brock
 */
public class AsyncCDIBeanManagerTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.async.test.bm.AsyncCDIBeanManagerTest";
  }

  public void testBeanManagerLookupInheritedScopeBean() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanDef<InheritedApplicationScopedBean> bean =
            IOC.getAsyncBeanManager().lookupBean(InheritedApplicationScopedBean.class, new QualB() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return QualB.class;
              }
            });

        assertNotNull("inherited application scoped bean did not lookup", bean);

        bean.getInstance(new CreationalCallback<InheritedApplicationScopedBean>() {
          @Override
          public void callback(final InheritedApplicationScopedBean beanInst) {
            assertNotNull("bean instance is null", beanInst);

            final DependentScopedBean bean1 = beanInst.getBean1();
            assertNotNull("bean1 is null", bean1);

            final DependentScopedBeanWithDependencies beanWithDependencies = beanInst.getBeanWithDependencies();
            assertNotNull("beanWithDependencies is null", beanWithDependencies);

            final DependentScopedBean bean2 = beanWithDependencies.getBean();
            assertNotSame("bean1 and bean2 should be different", bean1, bean2);

            bean.getInstance(new CreationalCallback<InheritedApplicationScopedBean>() {
              @Override
              public void callback(InheritedApplicationScopedBean beanInst2) {
                assertSame("bean is not observing application scope", beanInst, beanInst2);

                finishTest();
              }
            });
          }
        });
      }
    });

  }

  public void testBeanManagerLookupBeanFromAbstractRootType() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanDef<AbstractBean> bean = IOC.getAsyncBeanManager().lookupBean(AbstractBean.class);
        assertNotNull("did not find any beans matching", bean);

        bean.getInstance(new CreationalCallback<AbstractBean>() {
          @Override
          public void callback(final AbstractBean beanInst) {
            assertNotNull("bean instance is null", beanInst);

            assertTrue("bean is incorrect instance: " + beanInst.getClass(), beanInst instanceof InheritedFromAbstractBean);
            finishTest();
          }
        });
      }
    });
  }

  /**
   * This test effectively tests that the IOC container comprehends the full type hierarchy, considering both supertypes
   * and transverse interface types.
   */
  public void testBeanManagerLookupForOuterInterfaceRootType() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanDef<OuterBeanInterface> bean = IOC.getAsyncBeanManager().lookupBean(OuterBeanInterface.class);
        assertNotNull("did not find any beans matching", bean);

        bean.getInstance(new CreationalCallback<OuterBeanInterface>() {
          @Override
          public void callback(final OuterBeanInterface beanInst) {
            assertNotNull("bean instance is null", beanInst);
            assertTrue("bean is incorrect instance: " + beanInst.getClass(), beanInst instanceof InheritedFromAbstractBean);

            finishTest();
          }
        });
      }
    });
  }

  public void testBeanManagerLookupForOuterInterfacesOfNonAbstractType() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanDef<InterfaceC> beanC = IOC.getAsyncBeanManager().lookupBean(InterfaceC.class);
        assertNotNull("did not find any beans matching", beanC);

        final AsyncBeanDef<InterfaceD> beanD = IOC.getAsyncBeanManager().lookupBean(InterfaceD.class);
        assertNotNull("did not find any beans matching", beanD);

        finishTest();
      }
    });
  }

  public void testBeanManagerLookupForExtendedInterfaceType() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        // This should find ApplicationScopedBeanA, ApplicationScopedBeanB and ApplicationScopedBeanC
        final Collection<AsyncBeanDef<InterfaceRoot>> beans = IOC.getAsyncBeanManager().lookupBeans(InterfaceRoot.class);
        assertEquals("did not find all managed implementations of " + InterfaceRoot.class.getName(), 3, beans.size());

        // This should find ApplicationScopedBeanA and ApplicationScopedBeanB (InterfaceB extends InterfaceA)
        final Collection<AsyncBeanDef<InterfaceA>> beansB = IOC.getAsyncBeanManager().lookupBeans(InterfaceA.class);
        assertEquals("did not find both managed implementations of " + InterfaceA.class.getName(), 2, beansB.size());

        // This should find only ApplicationScopedBeanB
        final Collection<AsyncBeanDef<InterfaceB>> beansC = IOC.getAsyncBeanManager().lookupBeans(InterfaceB.class);
        assertEquals("did not find exactly one managed implementation of " + InterfaceB.class.getName(), 1, beansC.size());

        finishTest();
      }
    });
  }


  public void testBeanManagerAPIs() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanManager mgr = IOC.getAsyncBeanManager();
        final AsyncBeanDef<QualAppScopeBeanA> bean = mgr.lookupBean(QualAppScopeBeanA.class, new QualA() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return QualA.class;
          }
        });

        final Set<Annotation> a = bean.getQualifiers();
        assertEquals("there should be two qualifiers", 2, a.size());
        assertTrue("wrong qualifiers", annotationSetMatches(a, QualA.class, Any.class));

        finishTest();
      }
    });
  }

  public void testQualifiedLookup() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final QualA qualA = new QualA() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return QualA.class;
          }
        };

        final QualB qualB = new QualB() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return QualB.class;
          }
        };

        final Collection<AsyncBeanDef<CommonInterface>> beans
            = IOC.getAsyncBeanManager().lookupBeans(CommonInterface.class, QualifierUtil.ANY_ANNOTATION);
        assertEquals("wrong number of beans", 2, beans.size());

        final AsyncBeanDef<CommonInterface> beanA = IOC.getAsyncBeanManager().lookupBean(CommonInterface.class, qualA);
        assertNotNull("no bean found", beanA);

        beanA.getInstance(new CreationalCallback<CommonInterface>() {
          @Override
          public void callback(CommonInterface beanInstance) {
            assertTrue("wrong bean looked up", beanInstance instanceof QualAppScopeBeanA);

            final AsyncBeanDef<CommonInterface> beanB = IOC.getAsyncBeanManager().lookupBean(CommonInterface.class, qualB);
            assertNotNull("no bean found", beanB);

            beanB.getInstance(new CreationalCallback<CommonInterface>() {
              @Override
              public void callback(CommonInterface beanInstance) {
                assertTrue("wrong bean looked up", beanInstance instanceof QualAppScopeBeanB);
                finishTest();
              }
            });
          }
        });
      }
    });
  }

  public void testQualifierLookupWithAnnoAttrib() {
    final QualV qualApples = new QualV() {
      @Override
      public QualEnum value() {
        return QualEnum.APPLES;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return QualV.class;
      }

      @Override
      public int amount() {
        return 5;
      }
    };

    final QualV qualOranges = new QualV() {
      @Override
      public QualEnum value() {
        return QualEnum.ORANGES;
      }

      @Override
      public Class<? extends Annotation> annotationType() {
        return QualV.class;
      }

      @Override
      public int amount() {
        return 6;
      }
    };

    asyncTest(new Runnable() {
      @Override
      public void run() {
        final Collection<AsyncBeanDef<CommonInterfaceB>> beans
            = IOC.getAsyncBeanManager().lookupBeans(CommonInterfaceB.class, QualifierUtil.ANY_ANNOTATION);

        assertEquals("wrong number of beans", 2, beans.size());

        final AsyncBeanDef<CommonInterfaceB> beanA
            = IOC.getAsyncBeanManager().lookupBean(CommonInterfaceB.class, qualApples);
        assertNotNull("no bean found", beanA);

        beanA.getInstance(new CreationalCallback<CommonInterfaceB>() {
          @Override
          public void callback(CommonInterfaceB beanInstance) {
            assertTrue("wrong bean looked up", beanInstance instanceof QualParmAppScopeBeanApples);

            final AsyncBeanDef<CommonInterfaceB> beanB
                = IOC.getAsyncBeanManager().lookupBean(CommonInterfaceB.class, qualOranges);
            assertNotNull("no bean found", beanB);

            beanB.getInstance(new CreationalCallback<CommonInterfaceB>() {
              @Override
              public void callback(CommonInterfaceB beanInstance) {
                assertTrue("wrong bean looked up", beanInstance instanceof QualParmAppScopeBeanOranges);
                finishTest();
              }
            });
          }
        });
      }
    });
  }

  public void testQualifiedLookupFailure() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final LincolnBar wrongAnno = new LincolnBar() {
          @Override
          public Class<? extends Annotation> annotationType() {
            return LincolnBar.class;
          }
        };

        try {
          final AsyncBeanDef<CommonInterface> bean = IOC.getAsyncBeanManager().lookupBean(CommonInterface.class, QualifierUtil.ANY_ANNOTATION);
          fail("should have thrown an exception, but got: " + bean);
        }
        catch (IOCResolutionException e) {
          assertTrue("wrong exception thrown: " + e.getMessage(), e.getMessage().contains("Multiple beans matched"));
        }

        try {
          final AsyncBeanDef<CommonInterface> bean = IOC.getAsyncBeanManager().lookupBean(CommonInterface.class, wrongAnno);
          fail("should have thrown an exception, but got: " + bean);
        }
        catch (IOCResolutionException e) {
          assertTrue("wrong exception thrown", e.getMessage().contains("No beans matched"));
        }

        finishTest();
      }
    });
  }


  public void testLookupByName() {
    asyncTest(new Runnable() {
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

  public void testNameAvailableThroughInterfaceLookup() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        Collection<AsyncBeanDef<CreditCard>> beans = IOC.getAsyncBeanManager().lookupBeans(CreditCard.class);
        for (AsyncBeanDef<CreditCard> bean : beans) {
          if (bean.getBeanClass().getName().endsWith("Visa")) {
            assertEquals("visa", bean.getName());
          }
          else if (bean.getBeanClass().getName().endsWith("Amex")) {
            assertEquals("amex", bean.getName());
          }
          else {
            fail("Unexpected bean was returned from lookup: " + bean);
          }
        }

        finishTest();
      }
    });
  }

  public void testNameAvailableThroughConcreteTypeLookup() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        Collection<AsyncBeanDef<Visa>> beans = IOC.getAsyncBeanManager().lookupBeans(Visa.class);
        for (AsyncBeanDef<Visa> bean : beans) {
          assertNotNull("Missing name on " + bean, bean.getName());
        }

        finishTest();
      }
    });
  }

  public void testLookupAllBeans() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final Collection<AsyncBeanDef<Object>> beans = IOC.getAsyncBeanManager().lookupBeans(Object.class);

        assertTrue(!beans.isEmpty());
        finishTest();
      }
    });

  }

  private final QualA QUAL_A = new QualA() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return QualA.class;
    }
  };

  public void testLookupAllBeansQualified() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final Collection<AsyncBeanDef<Object>> beans
            = IOC.getAsyncBeanManager().lookupBeans(Object.class, QUAL_A);

        assertEquals("Unexpected number of beans matched. Actual results: " + beans, 1, beans.size());
        assertEquals(QualAppScopeBeanA.class, beans.iterator().next().getBeanClass());
        finishTest();
      }
    });

  }

  public void testReportedScopeCorrect() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanDef<ApplicationScopedBean> appScopeBean
            = IOC.getAsyncBeanManager().lookupBean(ApplicationScopedBean.class);
        final AsyncBeanDef<DependentScopedBean> dependentIOCBean
            = IOC.getAsyncBeanManager().lookupBean(DependentScopedBean.class);

        assertEquals(ApplicationScoped.class, appScopeBean.getScope());
        assertEquals(Dependent.class, dependentIOCBean.getScope());
        finishTest();
      }
    });
  }

  public void testAddingProgrammaticDestructionCallback() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        IOC.getAsyncBeanManager().lookupBean(DependentScopedBean.class)
            .newInstance(new CreationalCallback<DependentScopedBean>() {
              @Override
              public void callback(DependentScopedBean beanInstance) {
                class TestValueHolder {
                  boolean destroyed = false;
                }

                final TestValueHolder testValueHolder = new TestValueHolder();

                IOC.getAsyncBeanManager()
                    .addDestructionCallback(beanInstance, new DestructionCallback<Object>() {
                      @Override
                      public void destroy(Object bean) {
                        testValueHolder.destroyed = true;
                      }
                    });

                IOC.getAsyncBeanManager().destroyBean(beanInstance);

                assertEquals(true, testValueHolder.destroyed);

                finishTest();
              }
            });
      }
    });
  }

  /**
   * Tests that beans marked as Dependent scoped by an IOCExtension can still be forced into a different scope (in this
   * case, ApplicationScoped) when they are annotated as such.
   * <p/>
   * Besides this being a good idea on its own, both Errai UI Templates and Errai Navigation rely on this behaviour.
   * <p/>
   * NOTE: This looks really crazy written as an asynchronous test.
   */
  public void testNormalScopeOverridesDependent() {
    asyncTest(new Runnable() {
      @Override
      public void run() {
        final AsyncBeanQuery beanQuery = new AsyncBeanQuery();
        final AsyncBeanFuture<FoobieScopedBean> foobieScopedFuture1 = beanQuery.load(FoobieScopedBean.class);
        final AsyncBeanFuture<FoobieScopedBean> foobieScopedFuture2 = beanQuery.load(FoobieScopedBean.class);
        final AsyncBeanFuture<FoobieScopedOverriddenBean> foobieScopedOverriddenFuture1
            = beanQuery.load(FoobieScopedOverriddenBean.class);
        final AsyncBeanFuture<FoobieScopedOverriddenBean> foobieScopedOverriddenFuture2
            = beanQuery.load(FoobieScopedOverriddenBean.class);

        beanQuery.query(new Runnable() {
          @Override
          public void run() {
            final FoobieScopedBean foobieScopedBean1 = foobieScopedFuture1.get();
            final FoobieScopedBean foobieScopedBean2 = foobieScopedFuture2.get();
            final FoobieScopedOverriddenBean foobieScopedOverriddenBean1 = foobieScopedOverriddenFuture1.get();
            final FoobieScopedOverriddenBean foobieScopedOverriddenBean2 = foobieScopedOverriddenFuture2.get();

            assertNotNull(foobieScopedBean1);
            assertNotSame(foobieScopedBean1, foobieScopedBean2);
            assertNotNull(foobieScopedOverriddenBean1);
            assertSame(foobieScopedOverriddenBean1, foobieScopedOverriddenBean2);
            finishTest();
          }
        });
      }
    });
  }

  public void testBeanActivator() {
    final RefHolder<TestBeanActivator> activatorRef = new RefHolder<TestBeanActivator>();
    // This will happen synchronously as BeanActivators can not be annotated @LoadAsync
    IOC.getAsyncBeanManager().lookupBean(TestBeanActivator.class)
    .getInstance(new CreationalCallback<TestBeanActivator>() {
      @Override
      public void callback(TestBeanActivator beanInstance) {
        activatorRef.set(beanInstance);
      }
    });

    TestBeanActivator activator = activatorRef.get();
    activator.setActived(true);

    AsyncBeanDef<ActivatedBean> bean = IOC.getAsyncBeanManager().lookupBean(ActivatedBean.class);
    assertTrue(bean.isActivated());

    activator.setActived(false);
    assertFalse(bean.isActivated());

    AsyncBeanDef<ActivatedBeanInterface> qualifiedBean = IOC.getAsyncBeanManager().lookupBean(ActivatedBeanInterface.class);
    assertFalse(qualifiedBean.isActivated());

    activator.setActived(true);
    assertTrue(qualifiedBean.isActivated());
  }

  public void testBeanActiveByDefault() {
    AsyncBeanDef<DependentScopedBean> bean = IOC.getAsyncBeanManager().lookupBean(DependentScopedBean.class);
    assertTrue(bean.isActivated());
  }

  public void testInjectInstanceForSyncTypeWithAsyncEnabled() throws Exception {
    final ViaInstanceModule module = IOC.getBeanManager().lookupBean(ViaInstanceModule.class).getInstance();
    try {
      module.syncViaInstance.get();
    } catch (Throwable t) {
      throw new AssertionError("Should be able to get sync instance via Instance.get()", t);
    }
  }

  public void testErrorWhenUsingInstanceWithAsyncType() throws Exception {
    final ViaInstanceModule module = IOC.getBeanManager().lookupBean(ViaInstanceModule.class).getInstance();
    try {
      module.asyncViaInstance.get();
      fail("Should have failed from an unsatisfied dependency exception.");
    } catch (Throwable t) {
      assertTrue("The exception thrown did not have the @LoadAsync hint.", t.getMessage().contains("@LoadAsync"));
    }
  }

  private static boolean containsInstanceOf(final Collection<AsyncBeanDef> defs, final Class<?> clazz) {
    for (final AsyncBeanDef def : defs) {
      if (def.getType().equals(clazz)) return true;
    }
    return false;
  }
}
