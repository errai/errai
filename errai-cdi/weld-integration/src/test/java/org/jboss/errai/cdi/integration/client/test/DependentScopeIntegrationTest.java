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

package org.jboss.errai.cdi.integration.client.test;

import org.jboss.errai.cdi.integration.client.shared.ApplicationScopedBean;
import org.jboss.errai.cdi.integration.client.shared.ApplicationScopedBeanB;
import org.jboss.errai.cdi.integration.client.shared.DependentBeanCycleA;
import org.jboss.errai.cdi.integration.client.shared.DependentBeanCycleB;
import org.jboss.errai.cdi.integration.client.shared.DependentScopedBean;
import org.jboss.errai.cdi.integration.client.shared.DependentScopedBeanWithDependencies;
import org.jboss.errai.cdi.integration.client.shared.ServiceA;
import org.jboss.errai.cdi.integration.client.shared.ServiceB;
import org.jboss.errai.cdi.integration.client.shared.ServiceC;
import org.jboss.errai.cdi.integration.client.shared.TestBean;
import org.jboss.errai.cdi.integration.client.shared.TestOuterBean;
import org.jboss.errai.cdi.integration.client.shared.UnreferencedDependentRootBean;
import org.jboss.errai.common.client.api.extension.InitVotes;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class DependentScopeIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.InjectionTestModule";
  }

  public void testDependentBeanScope() {
    delayTestFinish(60000);

    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        ApplicationScopedBean beanA = IOC.getBeanManager()
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
    });

  }

  public void testDependentScopesWithTransverseDependentBeans() {
    delayTestFinish(60000);
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        TestOuterBean outBean = IOC.getBeanManager()
                .lookupBean(TestOuterBean.class).getInstance();

        assertNotNull("outer bean was null", outBean);

        TestBean testBean = outBean.getTestBean();
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

        ApplicationScopedBean applicationScopedBean = IOC.getBeanManager()
                .lookupBean(ApplicationScopedBean.class).getInstance();

        assertNotNull("ApplicationScopedBean was null", applicationScopedBean);

        ServiceC serviceC = IOC.getBeanManager()
                .lookupBean(ServiceC.class).getInstance();

        assertSame("ApplicationScopedBean should be same instance even in dependent scoped",
                serviceC.getBean(), applicationScopedBean);

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

        UnreferencedDependentRootBean applicationScopedBean = IOC.getBeanManager()
                .lookupBean(UnreferencedDependentRootBean.class).getInstance();

        assertNotNull("UnreferencedDependentRootBean was null", applicationScopedBean);
        assertNotNull("Dependent injection was null", applicationScopedBean.getBeanB());

        finishTest();
      }
    });
  }

  public void testDependentBeanCycle() {
    delayTestFinish(60000);
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {

        ApplicationScopedBeanB bean = IOC.getBeanManager()
                .lookupBean(ApplicationScopedBeanB.class).getInstance();

        assertNotNull("DependentBeanCycleA was null", bean);
        assertNotNull("dependentScopedBean.dependentBeanCycleB injection was null",
                bean.getDependentBeanCycleA());

        finishTest();
      }
    });
  }

}