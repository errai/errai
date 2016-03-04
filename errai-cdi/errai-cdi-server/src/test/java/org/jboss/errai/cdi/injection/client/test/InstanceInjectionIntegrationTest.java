/*
 * Copyright (C) 2011 Red Hat, Inc. and/or its affiliates.
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

package org.jboss.errai.cdi.injection.client.test;

import static org.jboss.errai.ioc.client.container.IOC.getBeanManager;

import javax.enterprise.inject.Instance;

import org.jboss.errai.cdi.injection.client.ApplicationScopedBeanA;
import org.jboss.errai.cdi.injection.client.DependentBeanA;
import org.jboss.errai.cdi.injection.client.DependentInstanceTestBean;
import org.jboss.errai.cdi.injection.client.InstanceTestBean;
import org.jboss.errai.cdi.injection.client.InterfaceA;
import org.jboss.errai.cdi.injection.client.UnmanagedBean;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;

/**
 * @author Mike Brock
 */
public class InstanceInjectionIntegrationTest extends AbstractErraiCDITest {
  {
    disableBus = true;
  }
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  public void testInstanceInjections() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();

    assertNotNull("InstanceTestBean is null", testBean);

    final Instance<ApplicationScopedBeanA> instanceApplicationScopedBean
            = testBean.getInjectApplicationScoped();
    assertNotNull("InstanceTestBean.Instance<ApplicationScopedBeanA> is null", instanceApplicationScopedBean);

    final Instance<DependentBeanA> instanceDependentBeanA = testBean.getInjectDependentBeanA();
    assertNotNull("InstanceTestBean.Instance<DependentBeanA> is null", instanceDependentBeanA);

    final ApplicationScopedBeanA a = instanceApplicationScopedBean.get();
    assertNotNull(a);

    final DependentBeanA b = instanceDependentBeanA.get();
    assertNotNull(b);

    final ApplicationScopedBeanA a1 = instanceApplicationScopedBean.get();
    final DependentBeanA b1 = instanceDependentBeanA.get();

    assertSame(a, a1);
    assertNotSame(b, b1);
    assertTrue(b1.isPostConstr());

    assertNotNull(b1.getBeanB());
    assertTrue(b1.getBeanB().isPostConstr());
  }

  public void testDependentInstanceInjection() {
    final DependentInstanceTestBean testBean
        = getBeanManager().lookupBean(DependentInstanceTestBean.class).getInstance();

    assertNotNull("DependentInstanceTestBean is null", testBean);

    final Instance<ApplicationScopedBeanA> instanceApplicationScopedBean
            = testBean.getInjectApplicationScoped();
    assertNotNull("DependentInstanceTestBean.Instance<ApplicationScopedBeanA> is null", instanceApplicationScopedBean);

    final Instance<DependentBeanA> instanceDependentBeanA = testBean.getInjectDependentBeanA();
    assertNotNull("DependentInstanceTestBean.Instance<DependentBeanA> is null", instanceDependentBeanA);

    final ApplicationScopedBeanA a = instanceApplicationScopedBean.get();
    assertNotNull(a);

    final DependentBeanA b = instanceDependentBeanA.get();
    assertNotNull(b);

    final ApplicationScopedBeanA a1 = instanceApplicationScopedBean.get();
    final DependentBeanA b1 = instanceDependentBeanA.get();

    assertSame(a, a1);
    assertNotSame(b, b1);
    assertTrue(b1.isPostConstr());

    assertNotNull(b1.getBeanB());
    assertTrue(b1.getBeanB().isPostConstr());
  }

  public void testIsUnsatisfied() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();
    assertNotNull("InstanceTestBean is null", testBean);

    final Instance<UnmanagedBean> instanceUnmanagedBean = testBean.getUnmanagedBean();
    assertNotNull("InstanceTestBean.Instance<UnmanagedBean> is null", instanceUnmanagedBean);
    assertTrue("Unmanaged bean should not be satisfied", instanceUnmanagedBean.isUnsatisfied());

    final Instance<ApplicationScopedBeanA> instanceApplicationScopedBean = testBean.getInjectApplicationScoped();
    assertFalse(instanceApplicationScopedBean.isUnsatisfied());

    final Instance<DependentBeanA> instanceDependentBeanA = testBean.getInjectDependentBeanA();
    assertFalse(instanceDependentBeanA.isUnsatisfied());
  }

  public void testIsAmbiguous() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();
    assertNotNull("InstanceTestBean is null", testBean);

    final Instance<InterfaceA> ambiguousBean = testBean.getAmbiguousBean();
    assertNotNull("InstanceTestBean.Instance<InterfaceA> is null", ambiguousBean);
    assertTrue(ambiguousBean.isAmbiguous());

    final Instance<UnmanagedBean> instanceUnmanagedBean = testBean.getUnmanagedBean();
    assertNotNull("InstanceTestBean.Instance<UnmanagedBean> is null", instanceUnmanagedBean);
    assertFalse(instanceUnmanagedBean.isAmbiguous());

    final Instance<ApplicationScopedBeanA> instanceApplicationScopedBean = testBean.getInjectApplicationScoped();
    assertFalse(instanceApplicationScopedBean.isAmbiguous());

    final Instance<DependentBeanA> instanceDependentBeanA = testBean.getInjectDependentBeanA();
    assertFalse(instanceDependentBeanA.isAmbiguous());
  }

  public void testIterator() {
    final DependentInstanceTestBean testBean
            = getBeanManager().lookupBean(DependentInstanceTestBean.class).getInstance();

    final Instance<DependentBeanA> injectedBean = testBean.getInjectDependentBeanA();

    assertTrue(injectedBean.iterator().hasNext());
    assertEquals(DependentBeanA.class, injectedBean.iterator().next().getClass());
  }
}
