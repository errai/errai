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
import org.jboss.errai.cdi.integration.client.shared.BeanInjectSelf;
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
public class CyclicDepsIntegrationTest extends AbstractErraiCDITest {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.integration.InjectionTestModule";
  }

  @Override
  public void gwtSetUp() throws Exception {
    super.gwtSetUp();
  }

  public void testBeanInjectsIntoSelf() {
    delayTestFinish(60000);

    InitVotes.registerOneTimeInitCallback(new Runnable() {
      @Override
      public void run() {
        BeanInjectSelf beanA = IOC.getBeanManager()
                .lookupBean(BeanInjectSelf.class).getInstance();

        assertNotNull(beanA);
        assertNotNull(beanA.getSelf());
        assertEquals(beanA.getInstance(), beanA.getSelf().getInstance());

        finishTest();
      }
    });

  }
}