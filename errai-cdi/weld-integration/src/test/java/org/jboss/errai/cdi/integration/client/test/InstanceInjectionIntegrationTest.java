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

import org.jboss.errai.cdi.integration.client.shared.ApplicationScopedBeanA;
import org.jboss.errai.cdi.integration.client.shared.DependentBeanA;
import org.jboss.errai.cdi.integration.client.shared.InstanceTestBean;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.enterprise.client.cdi.api.CDI;
import org.jboss.errai.ioc.client.container.IOC;

import javax.enterprise.inject.Instance;

/**
 * @author Mike Brock
 */
public class InstanceInjectionIntegrationTest extends AbstractErraiCDITest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.InstanceInjectorSupportTestModule";
  }

  public void testInstanceInjections() {
    delayTestFinish(60000);
    CDI.addPostInitTask(new Runnable() {
      @Override
      public void run() {
        InstanceTestBean testBean = IOC.getBeanManager().lookupBean(InstanceTestBean.class).getInstance();

        assertNotNull("InstanceTestBean is null", testBean);

        Instance<ApplicationScopedBeanA> instanceApplicationScopedBean = testBean.getInjectApplicationScoped();
        assertNotNull("InstanceTestBean.Instance<ApplicationScopedBeanA> is null", instanceApplicationScopedBean);

        Instance<DependentBeanA> instanceDependentBeanA = testBean.getInjectDependentBeanA();
        assertNotNull("InstanceTestBean.Instance<DependentBeanA> is null", instanceDependentBeanA);


        ApplicationScopedBeanA a = instanceApplicationScopedBean.get();
        assertNotNull(a);

        DependentBeanA b = instanceDependentBeanA.get();
        assertNotNull(b);

        ApplicationScopedBeanA a1 = instanceApplicationScopedBean.get();
        DependentBeanA b1 = instanceDependentBeanA.get();

        assertSame(a, a1);
        assertNotSame(b, b1);

        finishTest();
      }
    });
  }
}
