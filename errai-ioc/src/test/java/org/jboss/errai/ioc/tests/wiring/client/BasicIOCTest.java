/*
 * Copyright 2011 JBoss, a divison Red Hat, Inc
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

package org.jboss.errai.ioc.tests.wiring.client;

import org.jboss.errai.bus.client.ErraiBus;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.rebind.ioc.test.harness.IOCTestRunner;
import org.jboss.errai.ioc.tests.wiring.client.res.AfterTask;
import org.jboss.errai.ioc.tests.wiring.client.res.BeanManagerDependentBean;
import org.jboss.errai.ioc.tests.wiring.client.res.BeforeTask;
import org.jboss.errai.ioc.tests.wiring.client.res.HappyInspector;
import org.jboss.errai.ioc.tests.wiring.client.res.QualInspector;
import org.jboss.errai.ioc.tests.wiring.client.res.SetterInjectionBean;
import org.jboss.errai.ioc.tests.wiring.client.res.SimpleBean;
import org.jboss.errai.ioc.tests.wiring.client.res.SimpleBean2;
import org.jboss.errai.ioc.tests.wiring.client.res.TestResultsSingleton;
import org.jboss.errai.ioc.tests.wiring.client.res.TransverseDepService;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(IOCTestRunner.class)
public class  BasicIOCTest extends IOCClientTestCase {

  static {
    // Force classloading of SimpleBean so the package is discovered.
    Class<?> cls = SimpleBean.class;
  }

  @Override
  public String getModuleName() {
    return "org.jboss.errai.ioc.tests.wiring.IOCWiringTests";
  }

  public void testBasicInjectionScenarios() {
    SimpleBean simpleBean = IOC.getBeanManager().lookupBean(SimpleBean.class).getInstance();
    assertNotNull(simpleBean);

    assertEquals(ErraiBus.get(), simpleBean.getBus());
    assertEquals(ErraiBus.get(), simpleBean.getBus2());
    assertEquals(ErraiBus.get(), simpleBean.getBus3());
    assertEquals(ErraiBus.get(), simpleBean.getBus4());

    assertNotNull(simpleBean.getDispatcher());
    assertNotNull(simpleBean.getDispatcher2());
    assertNotNull(simpleBean.getDispatcher3());
    assertNotNull(simpleBean.getDispatcher4());

    TransverseDepService transverseDepService = IOC.getBeanManager().lookupBean(TransverseDepService.class).getInstance();

    assertNotNull("svcA is null", simpleBean.getSvcA());
    assertNotNull("svcB is null", simpleBean.getSvcB());
    assertTrue("injection of TransverseDepService into svcA returned different instance!",
            simpleBean.getSvcA().getSvc() == transverseDepService);

    assertTrue("injection of TransverseDepService into svcB returned different instance!",
                simpleBean.getSvcB().getSvc() == transverseDepService);

    assertTrue("@PostConstruct method not called", simpleBean.isPostConstructCalled());
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
    
    assertSame(IOC.getBeanManager(), bean.getBeanManager());
  }
}
