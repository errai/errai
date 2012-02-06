package org.jboss.errai.cdi.injection.client.test;


import org.jboss.errai.cdi.injection.client.InjectionTestModule;
import org.jboss.errai.ioc.client.IOCClientTestCase;
import org.jboss.errai.ioc.rebind.IOCTestRunner;
import org.junit.runner.RunWith;

/**
 * Tests CDI injection.
 *
 * @author Christian Sadilek <csadilek@redhat.com>
 */
@RunWith(IOCTestRunner.class)
public class InjectionIntegrationTest extends IOCClientTestCase {

  @Override
  public String getModuleName() {
    return "org.jboss.errai.cdi.injection.InjectionTestModule";
  }

  public void testInjections() {
    assertNotNull("Field injection of BeanA failed", InjectionTestModule.getInstance().getBeanA());
    assertNotNull("Field injection of BeanB in BeanA failed", InjectionTestModule.getInstance().getBeanA().getBeanB());
    
    assertNotNull("Field injection of BeanC failed", InjectionTestModule.getInstance().getBeanC());
    assertNotNull("Field injection of BeanB in BeanC failed", InjectionTestModule.getInstance().getBeanC().getBeanB());
    assertNotNull("Constructor injection of BeanD in BeanC", InjectionTestModule.getInstance().getBeanC().getBeanD());
  }


}