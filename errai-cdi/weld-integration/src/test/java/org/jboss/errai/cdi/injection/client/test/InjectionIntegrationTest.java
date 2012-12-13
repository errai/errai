package org.jboss.errai.cdi.injection.client.test;


import static org.jboss.errai.ioc.client.container.IOC.getBeanManager;

import org.jboss.errai.cdi.injection.client.Amex;
import org.jboss.errai.cdi.injection.client.CreditCardLover;
import org.jboss.errai.cdi.injection.client.InjectionTestModule;
import org.jboss.errai.cdi.injection.client.QaulParamDependentBeanApples;
import org.jboss.errai.cdi.injection.client.QaulParamDependentBeanOranges;
import org.jboss.errai.cdi.injection.client.Visa;
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

  public void testNewSemantics() {
    final InjectionTestModule module = IOC.getBeanManager()
        .lookupBean(InjectionTestModule.class).getInstance();

    assertFalse("BeanC1 should be @New instance", module.getBeanC() == module.getBeanC1());
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
}