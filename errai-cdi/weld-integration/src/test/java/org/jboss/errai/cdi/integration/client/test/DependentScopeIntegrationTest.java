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
import org.jboss.errai.cdi.integration.client.shared.DependentScopedBean;
import org.jboss.errai.cdi.integration.client.shared.DependentScopedBeanWithDependencies;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;

/**
 * @author Mike Brock
 */
public class DependentScopeIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.InjectionTests";
  }

  public void testDependentBeanScope() {
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        ApplicationScopedBean beanA = ApplicationScopedBean.getInstance();

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

    delayTestFinish(60000);
  }
}