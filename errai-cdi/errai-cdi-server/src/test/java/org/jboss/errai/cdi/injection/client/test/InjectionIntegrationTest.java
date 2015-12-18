/*
 * Copyright (C) 2015 Red Hat, Inc. and/or its affiliates.
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

import org.jboss.errai.cdi.injection.client.Amex;
import org.jboss.errai.cdi.injection.client.CreditCardLover;
import org.jboss.errai.cdi.injection.client.InjectionTestModule;
import org.jboss.errai.cdi.injection.client.ProducesProxiableOfAbstractType.NotConcrete;
import org.jboss.errai.cdi.injection.client.QaulParamDependentBeanApples;
import org.jboss.errai.cdi.injection.client.QaulParamDependentBeanOranges;
import org.jboss.errai.cdi.injection.client.Visa;
import org.jboss.errai.cdi.injection.client.ZFooAmex;
import org.jboss.errai.cdi.injection.client.ZFooVisa;
import org.jboss.errai.cdi.injection.client.Zoltron;
import org.jboss.errai.cdi.injection.client.ZoltronDependentBean;
import org.jboss.errai.cdi.injection.client.mvp.Contacts;
import org.jboss.errai.cdi.injection.client.qualifier.QualParmAppScopeBeanApples;
import org.jboss.errai.cdi.injection.client.qualifier.QualParmAppScopeBeanOranges;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.client.test.AbstractErraiIOCTest;

/**
 * Tests CDI injection.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InjectionIntegrationTest extends AbstractErraiIOCTest {
  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  public void testInjections() {
    final InjectionTestModule module = IOC.getBeanManager()
        .lookupBean(InjectionTestModule.class).getInstance();

    assertNotNull("Field injection of BeanA failed", module.getBeanA());
    assertNotNull("Field injection of BeanB in BeanA failed", module.getBeanA().getBeanB());
    assertNotNull("Field injection of BeanC failed", module.getBeanC());
  }

  public void testPostConstructFired() {
    final InjectionTestModule module = IOC.getBeanManager()
        .lookupBean(InjectionTestModule.class).getInstance();

    assertTrue("PostConstruct on InjectionTestModule did not fire", module.isPostConstructFired());
  }

  public void testMixedInjectionTypes() {
    final InjectionTestModule module = IOC.getBeanManager()
        .lookupBean(InjectionTestModule.class).getInstance();

    assertNotNull("Field injection of BeanB in BeanC failed", module.getBeanC().getBeanB());
    assertNotNull("Constructor injection of BeanD in BeanC", module.getBeanC().getBeanD());
  }

  public void testMvpInjections() {
    final Contacts mvpModule = IOC.getBeanManager().lookupBean(Contacts.class).getInstance();
    assertNotNull("Field injection of AppController failed", mvpModule.getAppController());
  }

  public void testQualifierBasedInjection() {
    final QaulParamDependentBeanApples instanceA
        = getBeanManager().lookupBean(QaulParamDependentBeanApples.class).getInstance();

    assertNotNull("bean is null", instanceA);
    assertTrue("incorrect instance injected",
        getBeanManager().getActualBeanReference(instanceA.getCommonInterfaceB()) instanceof QualParmAppScopeBeanApples);

    final QaulParamDependentBeanOranges instanceB
        = getBeanManager().lookupBean(QaulParamDependentBeanOranges.class).getInstance();

    assertNotNull("bean is null", instanceB);
    assertTrue("incorrect instance injected",
        getBeanManager().getActualBeanReference(instanceB.getCommonInterfaceB()) instanceof QualParmAppScopeBeanOranges);
  }

  public void testNamedBasedInjection() {
    final CreditCardLover creditCardLover
        = getBeanManager().lookupBean(CreditCardLover.class).getInstance();

    assertNotNull("bean is null", creditCardLover);

    assertTrue("bean should be an Amex", creditCardLover.getAmexCard() instanceof Amex);
    assertTrue("bean should be a Visa", creditCardLover.getVisaCard() instanceof Visa);
  }

  public void testResolutionOfParameterizedBeansExtFromAbstractClass() {
    final ZFooVisa zFooVisa
        = getBeanManager().lookupBean(ZFooVisa.class).getInstance();
    final ZFooAmex zFooAmex
        = getBeanManager().lookupBean(ZFooAmex.class).getInstance();

    assertNotNull(zFooVisa);
    assertNotNull(zFooAmex);

    assertNotNull(zFooVisa.getServiceXXX());
    assertSame(zFooVisa.getServiceXXX(), zFooAmex.getServiceXXX());
  }

  public void testNamedBasedInjectionFromProducedNamedBeans() {
    final ZoltronDependentBean zoltronDependentBean = getBeanManager().lookupBean(ZoltronDependentBean.class)
        .getInstance();

    assertNotNull("bean is null", zoltronDependentBean);

    final Zoltron alpha = zoltronDependentBean.getAlpha();
    assertNotNull("alpha is null", alpha);
    assertEquals("alpha", alpha.getName());

    final Zoltron beta = zoltronDependentBean.getBeta();
    assertNotNull("beta is null", beta);
    assertEquals("beta", beta.getName());


    final ZoltronDependentBean zoltronDependentBean2 = getBeanManager().lookupBean(ZoltronDependentBean.class)
        .getInstance();

    final Zoltron alpha2 = zoltronDependentBean2.getAlpha();
    final Zoltron beta2 = zoltronDependentBean2.getBeta();

    assertSame(alpha, alpha2);
    assertSame(beta, beta2);
  }

  public void testLoadingProxiedAbstractType() throws Exception {
    final NotConcrete bean = IOC.getBeanManager().lookupBean(NotConcrete.class).getInstance();
    assertFalse(bean.getValue());
    bean.setValueTrue();
    assertTrue(bean.getValue());
  }
}
