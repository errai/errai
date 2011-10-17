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
    assertNotNull("Failed to inject Bean A", InjectionTestModule.getInstance().getBeanA());
    assertNotNull("Failed to inject Bean B in Bean A", InjectionTestModule.getInstance().getBeanA().getBeanB());
    assertNotNull("Failed to inject Bean C", InjectionTestModule.getInstance().getBeanC());
    assertNotNull("Failed to inject Bean D in Bean C", InjectionTestModule.getInstance().getBeanC().getBeanD());
  }
}