package org.jboss.errai.cdi.injection.client.test;


import org.jboss.errai.cdi.injection.client.InjectionTestModule;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.client.container.IOC;
import org.jboss.errai.ioc.rebind.ioc.test.harness.IOCSimulatedTestRunner;
import org.junit.runner.RunWith;

/**
 * Tests CDI injection.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
public class InjectionIntegrationTest extends IOCClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  public void testInjections() {
    InjectionTestModule module = IOC.getBeanManager()
            .lookupBean(InjectionTestModule.class).getInstance();

    assertNotNull("Field injection of BeanA failed", module.getBeanA());
    assertNotNull("Field injection of BeanB in BeanA failed", module.getBeanA().getBeanB());
    
    assertNotNull("Field injection of BeanC failed", module.getBeanC());
    assertNotNull("Field injection of BeanB in BeanC failed", module.getBeanC().getBeanB());
    assertNotNull("Constructor injection of BeanD in BeanC", module.getBeanC().getBeanD());
    
    assertFalse("BeanC1 should be @New instance", module.getBeanC() == module.getBeanC1());

    assertTrue("PostConstruct on InjectionTestModule did not fire", module.isPostConstructFired());
  }
}