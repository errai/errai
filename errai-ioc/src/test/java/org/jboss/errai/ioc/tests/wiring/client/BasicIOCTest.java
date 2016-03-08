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

package org.jboss.errai.ioc.tests.wiring.client;

import java.util.Collection;
import java.util.List;

import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.QualifierUtil;
import org.jboss.errai.ioc.client.container.ClientBeanManager;
import org.jboss.errai.ioc.client.container.Factory;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.container.IOCEnvironment;
import org.jboss.errai.ioc.client.container.IOCResolutionException;
import org.jboss.errai.ioc.client.container.SyncBeanDef;
import org.jboss.errai.ioc.rebind.ioc.test.harness.IOCSimulatedTestRunner;
import org.jboss.errai.ioc.tests.wiring.client.res.ActivatedBean;
import org.jboss.errai.ioc.tests.wiring.client.res.ActivatedBeanInterface;
import org.jboss.errai.ioc.tests.wiring.client.res.AfterTask;
import org.jboss.errai.ioc.tests.wiring.client.res.BeanManagerDependentBean;
import org.jboss.errai.ioc.tests.wiring.client.res.BeforeTask;
import org.jboss.errai.ioc.tests.wiring.client.res.DependentOnInnerType;
import org.jboss.errai.ioc.tests.wiring.client.res.DependentWithPackageConstr;
import org.jboss.errai.ioc.tests.wiring.client.res.DependentWithPrivateConstr;
import org.jboss.errai.ioc.tests.wiring.client.res.DependentWithProtectedConstr;
import org.jboss.errai.ioc.tests.wiring.client.res.HappyInspector;
import org.jboss.errai.ioc.tests.wiring.client.res.IfaceProducer;
import org.jboss.errai.ioc.tests.wiring.client.res.ProxiableInjectableConstr;
import org.jboss.errai.ioc.tests.wiring.client.res.ProxiableInjectableConstrThrowsNPE;
import org.jboss.errai.ioc.tests.wiring.client.res.ProxiableNonPublicPostconstruct;
import org.jboss.errai.ioc.tests.wiring.client.res.ProxiableProtectedConstr;
import org.jboss.errai.ioc.tests.wiring.client.res.PublicInnerClassIface;
import org.jboss.errai.ioc.tests.wiring.client.res.QualInspector;
import org.jboss.errai.ioc.tests.wiring.client.res.SetterInjectionBean;
import org.jboss.errai.ioc.tests.wiring.client.res.SimpleBean;
import org.jboss.errai.ioc.tests.wiring.client.res.SimpleBean2;
import org.jboss.errai.ioc.tests.wiring.client.res.SimpleSingleton;
import org.jboss.errai.ioc.tests.wiring.client.res.SimpleSingleton2;
import org.jboss.errai.ioc.tests.wiring.client.res.TestBeanActivator;
import org.jboss.errai.ioc.tests.wiring.client.res.TestProviderDependentBean;
import org.jboss.errai.ioc.tests.wiring.client.res.TestResultsSingleton;
import org.jboss.errai.ioc.tests.wiring.client.res.TransverseDepService;
import org.junit.runner.RunWith;

import com.google.gwt.core.shared.GWT;

@RunWith(IOCSimulatedTestRunner.class)
public class BasicIOCTest extends IOCClientTestCase {

  static {
    // Force classloading of SimpleBean so the package is discovered.
    @SuppressWarnings("unused")
    Class<?> cls = SimpleBean.class;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }

  public void testBasicInjectionScenarios() {
    SimpleSingleton simpleSingleton = IOC.getBeanManager().lookupBean(SimpleSingleton.class).getInstance();
    SimpleSingleton2 simpleSingleton2 = IOC.getBeanManager().lookupBean(SimpleSingleton2.class).getInstance();

    assertNotNull(simpleSingleton);
    assertNotNull(simpleSingleton2);

    SimpleBean simpleBean = IOC.getBeanManager().lookupBean(SimpleBean.class).getInstance();
    assertNotNull(simpleBean);

    assertEquals(simpleSingleton, simpleBean.getSingletonA());
    assertEquals(simpleSingleton, simpleBean.getSingletonB());
    assertEquals(simpleSingleton, simpleBean.getSingletonC());
    assertEquals(simpleSingleton, simpleBean.getSuperSimpleSingleton());
    assertEquals(simpleSingleton2, simpleBean.getSingleton2());

    TransverseDepService transverseDepService = IOC.getBeanManager().lookupBean(TransverseDepService.class).getInstance();

    assertNotNull("svcA is null", simpleBean.getSvcA());
    assertNotNull("svcB is null", simpleBean.getSvcB());
    assertTrue("injection of TransverseDepService into svcA returned different instance!",
            simpleBean.getSvcA().getSvc() == transverseDepService);

    assertTrue("injection of TransverseDepService into svcB returned different instance!",
                simpleBean.getSvcB().getSvc() == transverseDepService);

    assertTrue("@PostConstruct method not called", simpleBean.isPostConstructCalled());
  }

  public void testNewInstanceFromSingleton() {
    SimpleSingleton simpleSingleton = IOC.getBeanManager().lookupBean(SimpleSingleton.class).getInstance();
    SimpleSingleton2 simpleSingleton2 = IOC.getBeanManager().lookupBean(SimpleSingleton2.class).getInstance();


    assertNotNull(simpleSingleton);
    assertNotNull(simpleSingleton2);

    SimpleBean simpleBean1 = IOC.getBeanManager().lookupBean(SimpleBean.class).getInstance();
    assertNotNull(simpleBean1);

    SimpleBean simpleBean2 = IOC.getBeanManager().lookupBean(SimpleBean.class).newInstance();

    assertNotSame("should have gotten new instance", simpleBean1, simpleBean2);

    assertEquals(simpleSingleton, simpleBean2.getSingletonA());
    assertEquals(simpleSingleton, simpleBean2.getSingletonB());
    assertEquals(simpleSingleton, simpleBean2.getSingletonC());
    assertEquals(simpleSingleton, simpleBean2.getSuperSimpleSingleton());
    assertEquals(simpleSingleton2, simpleBean2.getSingleton2());

    TransverseDepService transverseDepService = IOC.getBeanManager().lookupBean(TransverseDepService.class).getInstance();

    assertNotNull("svcA is null", simpleBean2.getSvcA());
    assertNotNull("svcB is null", simpleBean2.getSvcB());
    assertTrue("injection of TransverseDepService into svcA returned different instance!",
            simpleBean2.getSvcA().getSvc() == transverseDepService);

    assertTrue("injection of TransverseDepService into svcB returned different instance!",
                simpleBean2.getSvcB().getSvc() == transverseDepService);

    assertTrue("@PostConstruct method not called", simpleBean2.isPostConstructCalled());
  }

  public void testSetterMethodInjection() {
    SetterInjectionBean bean = IOC.getBeanManager().lookupBean(SetterInjectionBean.class)
            .getInstance();

    assertNotNull(bean);
    assertNotNull(bean.getServiceA());
    assertNotNull(bean.getServiceB());
  }

  public void testInjectionFromProvider() {
    SimpleBean2 simpleBean2 = IOC.getBeanManager().lookupBean(SimpleBean2.class).getInstance();

    assertEquals("FOO", simpleBean2.getMessage());
  }

  public void testInjectionFromProviderContextual() {
    SimpleBean2 simpleBean2 = IOC.getBeanManager().lookupBean(SimpleBean2.class).getInstance();

    assertEquals("FOO", simpleBean2.getMessage());
    assertEquals("java.lang.String", simpleBean2.getbSvc().get());
  }

  public void testInterfaceResolution() {
    HappyInspector happyInspector = IOC.getBeanManager().lookupBean(HappyInspector.class).getInstance();
    assertTrue(happyInspector.confirmHappiness());

    assertNotNull(happyInspector.getStringService());
    assertEquals("Hello", happyInspector.getStringService().get());

    assertNotNull(happyInspector.getIntegerService());
    assertEquals(new Integer(111), happyInspector.getIntegerService().get());

    assertNotNull(happyInspector.getLongService());
    assertEquals(new Long(1l), happyInspector.getLongService().get());
  }

  public void testQualifiers() {
    QualInspector qualInspector = QualInspector.INSTANCE;

    assertTrue(qualInspector.getaQualService().get() instanceof Integer);
    assertTrue(qualInspector.getbQualService().get() instanceof String);
  }

  public void testIOCTasks() {
    assertTrue("BeforeTask did not run", BeforeTask.ran);
    assertTrue("AfterTask did not run", AfterTask.ran);

    List<Class<?>> results = TestResultsSingleton.getItemsRun();
    assertTrue("BeforeTask did not run before AfterTask!",
            results.indexOf(BeforeTask.class) < results.indexOf(AfterTask.class));

  }

  public void testBeanManagerInjectable() {
    BeanManagerDependentBean bean = IOC.getBeanManager().lookupBean(BeanManagerDependentBean.class)
            .getInstance();

    ClientBeanManager beanManager = (GWT.<IOCEnvironment>create(IOCEnvironment.class).isAsync() ? IOC.getAsyncBeanManager() : IOC.getBeanManager());
    assertSame(beanManager, Factory.maybeUnwrapProxy(bean.getBeanManager()));
  }

  public void testProvidedValueLookup() {
    TestProviderDependentBean dependentBean = IOC.getBeanManager().lookupBean(TestProviderDependentBean.class)
        .getInstance();

    assertNotNull(dependentBean);
    assertNotNull(dependentBean.getTestProvidedIface());
    assertEquals("foo", dependentBean.getTestProvidedIface().getText());
  }

  public void testBeanActivator() {
    TestBeanActivator activator = IOC.getBeanManager().lookupBean(TestBeanActivator.class).getInstance();
    activator.setActived(true);

    SyncBeanDef<ActivatedBean> bean = IOC.getBeanManager().lookupBean(ActivatedBean.class);
    assertTrue(bean.isActivated());

    activator.setActived(false);
    assertFalse(bean.isActivated());

    SyncBeanDef<ActivatedBeanInterface> qualifiedBean = IOC.getBeanManager().lookupBean(ActivatedBeanInterface.class);
    assertFalse(qualifiedBean.isActivated());

    activator.setActived(true);
    assertTrue(qualifiedBean.isActivated());
  }

  public void testBeanActiveByDefault() {
    SyncBeanDef<BeanManagerDependentBean> bean = IOC.getBeanManager().lookupBean(BeanManagerDependentBean.class);
    assertTrue(bean.isActivated());
  }

  public void testInjectingStaticInnerClass() {
    DependentOnInnerType instance = IOC.getBeanManager().lookupBean(DependentOnInnerType.class).getInstance();
    assertNotNull(instance.getInner());
  }

  public void testLoadingBeanWithProxiableNonPublicPostConstruct() {
    final ProxiableNonPublicPostconstruct bean = IOC.getBeanManager().lookupBean(ProxiableNonPublicPostconstruct.class).getInstance();
    assertTrue(bean.getValue());
  }

  public void testProxyingWithProtectedConstructor() throws Exception {
    try {
      assertEquals("Package constructor should not have been invoked before instance lookup.", 0, ProxiableProtectedConstr.numPrivateConstrInvocations);
      IOC.getBeanManager().lookupBean(ProxiableProtectedConstr.class).getInstance();
      assertEquals("Private constructor was not used to create proxy.", 1, ProxiableProtectedConstr.numPrivateConstrInvocations);
    } catch (Throwable t) {
      throw new AssertionError("Could not construct instance of proxiable type with protected constructor.", t);
    }
  }

  public void testProxyingWithOnlyInjectableConstructor() throws Exception {
    try {
      IOC.getBeanManager().lookupBean(ProxiableInjectableConstr.class).getInstance();
    } catch (Throwable t) {
      throw new AssertionError("Could not construct instance of proxiable type with injectable constructor.", t);
    }
  }

  public void testErrorMessageWhenProxyingFailsWithOnlyInjectableConstructor() throws Exception {
    try {
      IOC.getBeanManager().lookupBean(ProxiableInjectableConstrThrowsNPE.class).getInstance();
      fail("Looking up an instance should have failed when creating proxy.");
    } catch (Throwable t) {
      assertTrue("The error message did not explain that the problem was with proxying.", t.getMessage().contains("proxy"));
    }
  }

  public void testDependentScopeWithPrivateConstr() throws Exception {
    try {
      IOC.getBeanManager().lookupBean(DependentWithPrivateConstr.class).getInstance();
    } catch (Throwable t) {
      throw new AssertionError("Could not create instance of bean with private constructor.", t);
    }
  }

  public void testDependentScopeWithPackageConstr() throws Exception {
    try {
      IOC.getBeanManager().lookupBean(DependentWithPackageConstr.class).getInstance();
    } catch (Throwable t) {
      throw new AssertionError("Could not create instance of bean with package constructor.", t);
    }
  }

  public void testDependentScopeWithProtectedConstr() throws Exception {
    try {
      IOC.getBeanManager().lookupBean(DependentWithProtectedConstr.class).getInstance();
    } catch (Throwable t) {
      throw new AssertionError("Could not create instance of bean with protected constructor.", t);
    }
  }

  public void testNoFactoryGeneratedForInnerClassOfNonPublicClass() throws Exception {
    final Collection<SyncBeanDef<PublicInnerClassIface>> foundBeans = IOC.getBeanManager().lookupBeans(PublicInnerClassIface.class, QualifierUtil.ANY_ANNOTATION);
    assertEquals(0, foundBeans.size());
  }

  public void testInterfaceStaticProducer() throws Exception {
    try {
      IOC.getBeanManager().lookupBean(IfaceProducer.class).getInstance();
    } catch (IOCResolutionException ex) {
      throw new AssertionError("Could not produce " + IfaceProducer.class.getSimpleName(), ex);
    }
  }
}
