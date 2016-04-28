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

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import javax.enterprise.inject.Instance;

import org.jboss.errai.cdi.injection.client.ApplicationScopedBeanA;
import org.jboss.errai.cdi.injection.client.DefaultImpl;
import org.jboss.errai.cdi.injection.client.DependentBeanA;
import org.jboss.errai.cdi.injection.client.DependentInstanceTestBean;
import org.jboss.errai.cdi.injection.client.IfaceWithMultipleDefaultImpls;
import org.jboss.errai.cdi.injection.client.IfaceWithMultipleImpls;
import org.jboss.errai.cdi.injection.client.InstanceTestBean;
import org.jboss.errai.cdi.injection.client.InterfaceA;
import org.jboss.errai.cdi.injection.client.OtherDefaultImpl;
import org.jboss.errai.cdi.injection.client.Qual1And2Impl;
import org.jboss.errai.cdi.injection.client.Qual1Impl;
import org.jboss.errai.cdi.injection.client.QualifiedInstanceModule;
import org.jboss.errai.cdi.injection.client.UnmanagedBean;
import org.jboss.errai.cdi.injection.client.qualifier.Qual2;
import org.jboss.errai.enterprise.client.cdi.AbstractErraiCDITest;
import org.jboss.errai.ioc.client.container.IOC;

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

  public void testSelectIsMoreSpecific() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleImpls> instance = module.ambiguousQual1;

    try {
      assertNotNull("Instance was not injected.", instance);
      assertExactlyTheseBeans(instance, Qual1Impl.class, Qual1And2Impl.class);
    } catch (AssertionError ae) {
      throw new AssertionError("Precondition failed.", ae);
    }

    final Instance<IfaceWithMultipleImpls> selectedInstance = instance.select(new Qual2() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Qual2.class;
      }
    });
    assertNotNull("Instance was not returned by select.", selectedInstance);
    assertExactlyTheseBeans(selectedInstance, Qual1And2Impl.class);
  }

  public void testSelectWithSubtypeIsMoreSpecific() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleImpls> instance = module.ambiguousQual1;

    try {
      assertNotNull("Instance was not injected.", instance);
      assertExactlyTheseBeans(instance, Qual1Impl.class, Qual1And2Impl.class);
    } catch (AssertionError ae) {
      throw new AssertionError("Precondition failed.", ae);
    }

    final Instance<IfaceWithMultipleImpls> selectedInstance = instance.select(IfaceWithMultipleImpls.class, new Qual2() {
      @Override
      public Class<? extends Annotation> annotationType() {
        return Qual2.class;
      }
    });
    assertNotNull("Instance was not returned by select.", selectedInstance);
    assertExactlyTheseBeans(selectedInstance, Qual1And2Impl.class);
  }

  public void testAmbiguousDefaultInstance() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleDefaultImpls> instance = module.ambiguousDefault;

    assertNotNull("Instance was not injected.", instance);
    assertExactlyTheseBeans(instance, DefaultImpl.class, OtherDefaultImpl.class);
  }

  public void testAmbiguousAnyInstance() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleDefaultImpls> instance = module.ambiguousAny;

    assertNotNull("Instance was not injected.", instance);
    assertExactlyTheseBeans(instance, DefaultImpl.class, OtherDefaultImpl.class);
  }

  public void testAmbiguousQualifiedInstance() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleImpls> instance = module.ambiguousQual1;

    assertNotNull("Instance was not injected.", instance);
    assertExactlyTheseBeans(instance, Qual1Impl.class, Qual1And2Impl.class);
  }

  public void testUnambiguousAndSatisfiedDefaultInstance() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleImpls> instance = module.gettableDefault;

    assertNotNull("Instance was not injected.", instance);
    assertExactlyTheseBeans(instance, DefaultImpl.class);
    assertGetIsType(instance, DefaultImpl.class);
  }

  public void testUnambiguousAndSatisfiedExplicitDefaultInstance() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleImpls> instance = module.gettableExplicitDefault;

    assertNotNull("Instance was not injected.", instance);
    assertExactlyTheseBeans(instance, DefaultImpl.class);
    assertGetIsType(instance, DefaultImpl.class);
  }

  public void testUnambiguousAndSatisfiedQual1AndBInstance() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleImpls> instance = module.gettableQual1AndB;

    assertNotNull("Instance was not injected.", instance);
    assertExactlyTheseBeans(instance, Qual1And2Impl.class);
    assertGetIsType(instance, Qual1And2Impl.class);
  }

  public void testUnsatisfiedQual1Instance() throws Exception {
    final QualifiedInstanceModule module = IOC.getBeanManager().lookupBean(QualifiedInstanceModule.class).getInstance();
    final Instance<IfaceWithMultipleDefaultImpls> instance = module.unsatisfiedQual1;

    assertNotNull("Instance was not injected.", instance);
    assertExactlyTheseBeans(instance);
  }

  public void testGetForInjectedInstances() {
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

  public void testScopingBehaviourViaGet() {
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

  public void testUnmangedBeanIsUnsatisfied() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();
    assertNotNull("InstanceTestBean is null", testBean);

    final Instance<UnmanagedBean> instanceUnmanagedBean = testBean.getUnmanagedBean();
    assertNotNull("InstanceTestBean.Instance<UnmanagedBean> is null", instanceUnmanagedBean);
    assertTrue("Unmanaged bean should not be satisfied", instanceUnmanagedBean.isUnsatisfied());
  }

  public void testSatisfiedScopedBeansAreNotUnsatisfied() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();
    assertNotNull("InstanceTestBean is null", testBean);

    final Instance<ApplicationScopedBeanA> instanceApplicationScopedBean = testBean.getInjectApplicationScoped();
    assertFalse(instanceApplicationScopedBean.isUnsatisfied());

    final Instance<DependentBeanA> instanceDependentBeanA = testBean.getInjectDependentBeanA();
    assertFalse(instanceDependentBeanA.isUnsatisfied());
  }

  public void testAmbgiousBeanIsNotUnsatisfied() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();
    assertNotNull("InstanceTestBean is null", testBean);

    final Instance<InterfaceA> instanceAmbiguousBean = testBean.getAmbiguousBean();
    assertNotNull("InstanceTestBean.Instance<InterfaceA> is null", instanceAmbiguousBean);
    assertFalse("Ambiguous bean should be satisfied", instanceAmbiguousBean.isUnsatisfied());
  }

  public void testAmbiguousInstanceIsAmbiguous() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();
    assertNotNull("InstanceTestBean is null", testBean);

    final Instance<InterfaceA> ambiguousBean = testBean.getAmbiguousBean();
    assertNotNull("InstanceTestBean.Instance<InterfaceA> is null", ambiguousBean);
    assertTrue(ambiguousBean.isAmbiguous());
  }

  public void testUnmanagedBeanIsNotAmbiguous() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();
    assertNotNull("InstanceTestBean is null", testBean);

    final Instance<UnmanagedBean> instanceUnmanagedBean = testBean.getUnmanagedBean();
    assertNotNull("InstanceTestBean.Instance<UnmanagedBean> is null", instanceUnmanagedBean);
    assertFalse(instanceUnmanagedBean.isAmbiguous());
  }

  public void testUnambiguousScopedBeansAreNotAmbiguous() {
    final InstanceTestBean testBean = getBeanManager().lookupBean(InstanceTestBean.class).getInstance();
    assertNotNull("InstanceTestBean is null", testBean);

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

  private <T> void assertExactlyTheseBeans(final Instance<T> instance, final Class<?>... clazzes) {
    final Set<Class<?>> givenTypes = new HashSet<>(Arrays.asList(clazzes));
    final Set<Class<?>> extraTypes = new HashSet<>();
    for (final T bean : instance) {
      if (!givenTypes.remove(bean.getClass())) {
        extraTypes.add(bean.getClass());
      }
    }

    if (!(givenTypes.isEmpty() && extraTypes.isEmpty())) {
      fail("Instance did not contain the following: " + givenTypes + "\nInstance had the following unexpected: " + extraTypes);
    }
  }

  private <T> void assertGetIsType(final Instance<T> instance, final Class<?> clazz) {
    try {
      final T bean = instance.get();
      assertNotNull("Instance.get returned null.", bean);
      assertEquals("Bean was not of the expected type.", clazz, bean.getClass());
    } catch (AssertionError ae) {
      throw ae;
    } catch (Throwable t) {
      throw new AssertionError("Could not use Instance.get. Encountered an error.", t);
    }
  }
}
