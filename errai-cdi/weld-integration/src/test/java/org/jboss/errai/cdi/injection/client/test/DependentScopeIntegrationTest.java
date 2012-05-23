/*
 * Copyright 2011 JBoss, by Red Hat, Inc
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jboss.errai.cdi.injection.client.test;

import java.util.HashSet;
import java.util.Set;

import org.jboss.errai.cdi.injection.client.ApplicationScopedBean;
import org.jboss.errai.cdi.injection.client.ApplicationScopedBeanB;
import org.jboss.errai.cdi.injection.client.Bean;
import org.jboss.errai.cdi.injection.client.BeanInjectsNonModuleDependentBean;
import org.jboss.errai.cdi.injection.client.BeanInjectsNonModuleDependentBeanB;
import org.jboss.errai.cdi.injection.client.DepScopedBeanWithASBeanDep;
import org.jboss.errai.cdi.injection.client.DependentBeanCycleA;
import org.jboss.errai.cdi.injection.client.DependentBeanCycleB;
import org.jboss.errai.cdi.injection.client.DependentScopedBean;
import org.jboss.errai.cdi.injection.client.DependentScopedBeanWithDependencies;
import org.jboss.errai.cdi.injection.client.DestroyA;
import org.jboss.errai.cdi.injection.client.LincolnCat;
import org.jboss.errai.cdi.injection.client.OuterBean;
import org.jboss.errai.cdi.injection.client.ServiceA;
import org.jboss.errai.cdi.injection.client.ServiceB;
import org.jboss.errai.cdi.injection.client.ServiceC;
import org.jboss.errai.cdi.injection.client.UnreferencedDependentRootBean;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

/**
 * @author Mike Brock
 */
public class DependentScopeIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  @Override
  public void gwtSetUp() throws Exception {
    DependentBeanCycleB.instanceCount = 1;

    super.gwtSetUp();
  }

  public void testDependentBeanScope() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        try {
          final ApplicationScopedBean beanA = IOC.getBeanManager()
                  .lookupBean(ApplicationScopedBean.class).getInstance();

          DependentScopedBean b1 = beanA.getBean1();
          DependentScopedBean b2 = beanA.getBean2();
          DependentScopedBean b3 = beanA.getBean3();
          DependentScopedBeanWithDependencies b4 = beanA.getBeanWithDependencies();

          assertTrue("dependent scoped semantics broken", b2.getInstance() > b1.getInstance());
          assertTrue("dependent scoped semantics broken", b3.getInstance() > b2.getInstance());

          assertNotNull("dependent scoped bean with injections was not injected", b4);
          assertNotNull("dependent scoped beans own injections not injected", b4.getBean());
          assertTrue("dependent scoped semantics broken", b4.getBean().getInstance() > b3.getInstance());

          finishTest();
        }
        catch (Throwable t) {
          t.printStackTrace();
        }
      }
    });

  }

  public void testDependentScopesWithTransverseDependentBeans() {
    delayTestFinish(60000);
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final OuterBean outBean = IOC.getBeanManager()
                .lookupBean(OuterBean.class).getInstance();

        assertNotNull("outer bean was null", outBean);

        Bean testBean = outBean.getTestBean();
        assertNotNull("outBean.getTestBean() returned null", testBean);

        ServiceA serviceA = testBean.getServiceA();
        ServiceB serviceB = testBean.getServiceB();
        ServiceC serviceC = testBean.getServiceC();

        assertNotNull("serviceA is null", serviceA);
        assertNotNull("serviceB is null", serviceB);
        assertNotNull("serviceC is null", serviceC);

        ServiceC serviceC1 = serviceA.getServiceC();
        ServiceC serviceC2 = serviceB.getServiceC();

        assertNotNull("serviceC in serviceA is null", serviceC1);
        assertNotNull("serviceC in serviceB is null", serviceC2);

        Set<String> testDependentScope = new HashSet<String>();
        testDependentScope.add(serviceC.getName());
        testDependentScope.add(serviceC1.getName());
        testDependentScope.add(serviceC2.getName());

        assertEquals("ServiceC should have been instantiated 3 times", 3, testDependentScope.size());

        finishTest();
      }
    });
  }

  public void testDependentScopeDoesNotViolateBroaderApplicationScope() {
    delayTestFinish(60000);
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final ApplicationScopedBean applicationScopedBean = IOC.getBeanManager()
                .lookupBean(ApplicationScopedBean.class).getInstance();

        assertNotNull("ApplicationScopedBean was null", applicationScopedBean);

        final ServiceC serviceC = IOC.getBeanManager()
                .lookupBean(ServiceC.class).getInstance();

        assertEquals("ApplicationScopedBean should be same instance even in dependent scoped",
                applicationScopedBean.getBeanId(), serviceC.getBean().getBeanId());

        finishTest();
      }
    });
  }

  /**
   * Tests that a dependent scoped bean, which is not referenced by other beans, is accessible from the BeanManager.
   * Additionally, this test also ensures that a dependent scoped bean is resolveable behind an interface that
   * it implements and that the interface is an innerclass of another bean.
   */
  public void testUnreferencedDependentRootBeanAccessible() {
    delayTestFinish(60000);
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {

        final UnreferencedDependentRootBean applicationScopedBean = IOC.getBeanManager()
                .lookupBean(UnreferencedDependentRootBean.class).getInstance();

        assertNotNull("UnreferencedDependentRootBean was null", applicationScopedBean);
        assertNotNull("Dependent injection was null", applicationScopedBean.getBeanB());

        finishTest();
      }
    });
  }

  public void testDependentBeanCycleFromApplicationScopedRoot() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        final ApplicationScopedBeanB bean = IOC.getBeanManager()
                .lookupBean(ApplicationScopedBeanB.class).getInstance();

        assertNotNull("DependentBeanCycleA was null", bean);
        assertNotNull("dependentScopedBean.dependentBeanCycleA injection was null",
                bean.getDependentBeanCycleA());
        assertNotNull("dependentScopedBean.dependentBeanCycleA.dependentBeanCycleB was null",
                bean.getDependentBeanCycleA().getDependentBeanCycleB());
        assertEquals("there should have been only one instantiation of DependentBeanCycleB",
                1, bean.getDependentBeanCycleA().getDependentBeanCycleB().getInstance());

        finishTest();
      }
    });
  }

  public void testDependentBeanCycleFromDependentRoot() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        DependentBeanCycleB.instanceCount = 1;

        final DependentBeanCycleB bean = IOC.getBeanManager()
                .lookupBean(DependentBeanCycleB.class).getInstance();

        assertNotNull("bean was null", bean);
        assertNotNull("bean.dependentBeanCycleA injection was null",
                bean.getDependentBeanCycleA());
        assertNotNull("dependentScopedBean.dependentBeanCycleB.dependentBeanCycleA was null",
                bean.getDependentBeanCycleA().getDependentBeanCycleB());
        assertEquals("there should have been only one instantiation of DependentBeanCycleB",
                1, bean.getDependentBeanCycleA().getDependentBeanCycleB().getInstance());

        DependentBeanCycleB.instanceCount = 1;

        final DependentBeanCycleA beanA = IOC.getBeanManager()
                .lookupBean(DependentBeanCycleA.class).getInstance();

        assertNotNull("beanA was null", beanA);
        assertNotNull("dependentScopedBean.dependentBeanCycleB injection was null",
                beanA.getDependentBeanCycleB());
        assertNotNull("dependentScopedBean.dependentBeanCycleB.dependentBeanCycleA was null",
                beanA.getDependentBeanCycleB().getDependentBeanCycleA());
        assertEquals("there should have been only two instantiations of DependentBeanCycleB",
                1, beanA.getDependentBeanCycleB().getDependentBeanCycleA().getDependentBeanCycleB().getInstance());

        finishTest();
      }
    });
  }


  public void testDependentBeanCycleWithPreDestroy() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {

      @Override
      public void run() {
        final DestroyA bean = IOC.getBeanManager()
                .lookupBean(DestroyA.class).getInstance();

        IOC.getBeanManager().destroyBean(bean);

        assertTrue("predestroy method not called!", bean.isDestroyed());
        assertTrue("predestroy method not called", bean.getTestDestroyB().isDestroyed());

        finishTest();
      }
    });
  }

  public void testDependentBeanWithProducerDependency() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {

      @Override
      public void run() {
        final LincolnCat bean = IOC.getBeanManager()
                .lookupBean(LincolnCat.class).getInstance();

        assertNotNull("no instance returned for bean", bean);
        assertNotNull("value not injected", bean.getBar());
        assertEquals("wrong value injected", "bar", bean.getBar());

        finishTest();
      }
    });
  }

  public void testDependentScopedBeanWithAppScopedDependencyDestroy() {
    InitVotes.registerOneTimeInitCallback(new Runnable() {

      @Override
      public void run() {
        final DepScopedBeanWithASBeanDep bean = IOC.getBeanManager()
                .lookupBean(DepScopedBeanWithASBeanDep.class).getInstance();

        assertNotNull("no instance returned for bean", bean);
        assertNotNull("ApplicationScopedBean not injected", bean.getApplicationScopedBean());

        IOC.getBeanManager().destroyBean(bean);

        assertTrue("pre-destroy method not called", bean.isPreDestroyCalled());
        assertFalse("ApplicationScopedBean's predestruct method must NOT be called",
                bean.getApplicationScopedBean().isPreDestroyCalled());

        assertFalse("bean should no longer be managed", IOC.getBeanManager().isManaged(bean));

        finishTest();
      }
    });
  }

  public void testNonModuleTranslatableClassInjectableAsDependent() {
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        final BeanInjectsNonModuleDependentBean bean = IOC.getBeanManager()
                .lookupBean(BeanInjectsNonModuleDependentBean.class).getInstance();

        assertNotNull("no instance returned for bean", bean);
        assertNotNull("non-module dependent bean not injected", bean.getList());
        assertEquals("wrong number of elements in list", 2, bean.getList().size());
        assertEquals("wrong element", "foo", bean.getList().get(0));
        assertEquals("wrong element", "bar", bean.getList().get(1));

        finishTest();
      }
    });
  }

  public void testNonModuleTranslatableClassInjectableAsDependentWithAliasedInjectionPoint() {
    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        final BeanInjectsNonModuleDependentBeanB bean = IOC.getBeanManager()
                .lookupBean(BeanInjectsNonModuleDependentBeanB.class).getInstance();

        assertNotNull("no instance returned for bean", bean);
        assertNotNull("non-module dependent bean not injected", bean.getFunArrayListOfString());
        assertEquals("wrong number of elements in list", 2, bean.getFunArrayListOfString().size());
        assertEquals("wrong element", "foo", bean.getFunArrayListOfString().get(0));
        assertEquals("wrong element", "bar", bean.getFunArrayListOfString().get(1));

        finishTest();
      }
    });
  }
}